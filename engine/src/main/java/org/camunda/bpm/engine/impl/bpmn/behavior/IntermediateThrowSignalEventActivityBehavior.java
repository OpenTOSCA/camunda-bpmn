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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {    
  
  private final static Logger LOGGER = Logger.getLogger(IntermediateThrowSignalEventActivityBehavior.class.getName());
  
  protected final EventSubscriptionDeclaration signalDefinition;

  public IntermediateThrowSignalEventActivityBehavior(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {
    final EventSubscriptionManager eventSubscriptionManager = Context.getCommandContext().getEventSubscriptionManager();
    
    // notify all executions waiting of this signal
    List<SignalEventSubscriptionEntity> catchSignalEventSubscription = eventSubscriptionManager
      .findSignalEventSubscriptionsByEventName(signalDefinition.getEventName());
    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : catchSignalEventSubscription) {
      if(isActiveEventSubscription(signalEventSubscriptionEntity)){
        signalEventSubscriptionEntity.eventReceived(null, signalDefinition.isAsync());
      }
    }
    
    // start new process instances of process definitions that can started by this signal 
    List<SignalEventSubscriptionEntity> startSignalEventSubscriptions = eventSubscriptionManager
        .findSignalStartEventSubscriptionsByName(signalDefinition.getEventName());
    for (SignalEventSubscriptionEntity eventSubscription : startSignalEventSubscriptions) {
      startProcessInstanceBySignal(eventSubscription);
    }
    
    leave(execution);        
  }

  private boolean isActiveEventSubscription(SignalEventSubscriptionEntity signalEventSubscriptionEntity) {
    ExecutionEntity execution = signalEventSubscriptionEntity.getExecution();
    return !execution.isEnded() && !execution.isCanceled();
  }

  protected void startProcessInstanceBySignal(SignalEventSubscriptionEntity eventSubscription) {
    String processDefinitionId = eventSubscription.getConfiguration();
    ensureNotNull("Configuration of signal start event subscription '" + eventSubscription.getId() + "' contains no process definition id.",
        processDefinitionId);

    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null || processDefinition.isSuspended()) {
      // ignore event subscription
      LOGGER.log(Level.FINE, "Found event subscription with {0} but process definition {1} could not be found.",
          new Object[] { eventSubscription, processDefinitionId });
    } else {
      
      ActivityImpl signalStartEvent = processDefinition.findActivity(eventSubscription.getActivityId());
      PvmProcessInstance processInstance = processDefinition.createProcessInstanceForInitial(signalStartEvent);
      processInstance.start();
    }
  } 
  
}
