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

package org.camunda.bpm.engine.test.bpmn.event.signal;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.AssertionFailedError;

/**
 * Parse an invalid process definition and assert the error message.
 * 
 * @author Philipp Ossler
 */
@RunWith(Parameterized.class)
public class SignalEventParseInvalidProcessTest {

  private static final String PROCESS_DEFINITION_DIRECTORY = "org/camunda/bpm/engine/test/bpmn/event/signal/";
  
  @Parameters(name = "{index}: process definition = {0}, expected error message = {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
        { "InvalidProcessWithDuplicateSignalNames.bpmn20.xml", "duplicate signal name" },
        { "InvalidProcessWithNoSignalName.bpmn20.xml", "signal with id 'alertSignal' has no name" },
        { "InvalidProcessWithSignalNoId.bpmn20.xml", "signal must have an id" },
        { "InvalidProcessWithSignalNoRef.bpmn20.xml", "signalEventDefinition does not have required property 'signalRef'" },
        { "InvalidProcessWithMultipleSignalStartEvents.bpmn20.xml", "Cannot have more than one signal event subscription with name 'signal'" },
        { "InvalidProcessWithMultipleInterruptingSignalEventSubProcesses.bpmn20.xml", "Cannot have more than one signal event subscription with name 'alert'" }
    });
  }

  @Parameter(0)
  public String processDefinitionResource;
  
  @Parameter(1)
  public String expectedErrorMessage;

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule(PluggableProcessEngineTestCase.getProcessEngine());

  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void initServices() {    
    managementService = rule.getManagementService();
    repositoryService = rule.getRepositoryService();
    runtimeService = rule.getRuntimeService();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration();
  }
  
  @Test
  public void testParseInvalidProcessDefinition() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource(PROCESS_DEFINITION_DIRECTORY + processDefinitionResource)
        .deploy();
      
      fail("exception expected: " + expectedErrorMessage);
    } catch (Exception e) {
      assertTextPresent(expectedErrorMessage, e.getMessage());
    }
  }
  
  public void assertTextPresent(String expected, String actual) {
    if (actual == null || !actual.contains(expected)) {
      throw new AssertionFailedError("expected presence of [" + expected + "], but was [" + actual + "]");
    }
  }
}
