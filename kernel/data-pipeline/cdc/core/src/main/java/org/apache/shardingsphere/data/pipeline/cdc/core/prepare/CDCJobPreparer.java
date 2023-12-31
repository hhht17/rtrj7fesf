/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.cdc.core.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.job.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.prepare.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * CDC job preparer.
 */
@Slf4j
public final class CDCJobPreparer {
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    /**
     * Do prepare work.
     *
     * @param jobItemContext job item context
     */
    public void initTasks(final CDCJobItemContext jobItemContext) {
        Optional<InventoryIncrementalJobItemProgress> jobItemProgress = jobAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        if (!jobItemProgress.isPresent()) {
            jobAPI.persistJobItemProgress(jobItemContext);
        }
        if (jobItemContext.isStopping()) {
            PipelineJobCenter.stop(jobItemContext.getJobId());
            return;
        }
        initIncrementalTasks(jobItemContext);
        CDCJobConfiguration jobConfig = jobItemContext.getJobConfig();
        if (jobConfig.isFull()) {
            initInventoryTasks(jobItemContext);
        }
    }
    
    private void initInventoryTasks(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        // TODO channel requires a new implementation
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), inventoryDumperConfig, taskConfig.getImporterConfig());
        List<InventoryTask> allInventoryTasks = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        jobItemContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(initIncremental, taskConfig.getDumperConfig(), jobItemContext.getDataSourceManager()));
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobItemContext.getSourceMetaDataLoader();
        ExecuteEngine incrementalExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalExecuteEngine();
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getImporterConfig().getConcurrency(), taskConfig.getDumperConfig(), taskConfig.getImporterConfig(),
                pipelineChannelCreator, jobItemContext.getImporterConnector(), sourceMetaDataLoader, incrementalExecuteEngine, jobItemContext);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
}
