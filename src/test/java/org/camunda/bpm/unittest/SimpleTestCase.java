/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.util.LogUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleTestCase {

	// enable more detailed logging
	static {
	    LogUtil.readJavaUtilLoggingConfigFromClasspath();
	}

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn", "calledProcessWithAsyncServiceTask.bpmn"})
  public void shouldExecuteProcess() throws InterruptedException {

    RuntimeService runtimeService = rule.getRuntimeService();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");
    assertFalse("Process instance should not be ended", pi.isEnded());
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    
    ManagementService managementService = rule.getManagementService();
    assertEquals(2, managementService.createJobQuery().count());

    for (Job job : managementService.createJobQuery().list()) {
		System.out.println(job);
	}
    
    Thread.sleep(3000);

//    executeAvailableJobs();
    executeAvailableJobsMultiThreaded();
    // see also: org.camunda.bpm.engine.test.concurrency.CompetingSignalsTest

    Thread.sleep(3000);
    for (Job job : managementService.createJobQuery().list()) {
		System.out.println(job);
	}
    assertEquals(0, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().count());
    // now the process instance should be ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  private void executeAvailableJobsMultiThreaded() {
	  ManagementService managementService = rule.getManagementService();
	  List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();
	  
	  if (jobs.isEmpty()) {
		  return;
	  }
	  
	  List<Thread> threads = new ArrayList<Thread>();
	  
	  for (Job job : jobs) {
		  threads.add(
				  new Thread(
						  new ExecuteJobsRunnable(
								  Arrays.asList(job.getId()),
								  (ProcessEngineImpl) rule.getProcessEngine()
						  )
				  )
		  );
	  }
	  
	  for (Thread thread : threads) {
		thread.start();
	}
	  
//	  executeAvailableJobsMultiThreaded();
  }

  public void executeAvailableJobs() {
	  ManagementService managementService = rule.getManagementService();
	  List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();
	  
	  if (jobs.isEmpty()) {
		  return;
	  }
	  
	  for (Job job : jobs) {
		  try {
			  managementService.executeJob(job.getId());
		  } catch (Exception e) {};
	  }
	  
	  executeAvailableJobs();
  }
}
