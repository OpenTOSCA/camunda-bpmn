/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.standalone.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;

/**
 * @author Roman Smirnov
 *
 */
public class TaskDecoratorTest extends PluggableProcessEngineTestCase {

  protected TaskEntity task;
  protected TaskDefinition taskDefinition;
  protected TaskDecorator taskDecorator;
  protected ExpressionManager expressionManager;

  public void setUp() {
    task = (TaskEntity) taskService.newTask();
    taskService.saveTask(task);

    expressionManager = processEngineConfiguration
        .getExpressionManager();

    taskDefinition = new TaskDefinition(null);
    taskDecorator = new TaskDecorator(taskDefinition, expressionManager);
  }

  public void tearDown() {
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new DeleteTaskCommand(task));
  }

  protected void decorate(TaskEntity task, TaskDecorator decorator) {
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new DecorateTaskCommand(task, decorator));
  }

  public void testDecorateName() {
    // given
    String aTaskName = "A Task Name";
    Expression nameExpression = expressionManager.createExpression(aTaskName);
    taskDefinition.setNameExpression(nameExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aTaskName, task.getName());
  }

  public void testDecorateNameFromVariable() {
    // given
    String aTaskName = "A Task Name";
    taskService.setVariable(task.getId(), "taskName", aTaskName);

    Expression nameExpression = expressionManager.createExpression("${taskName}");
    taskDefinition.setNameExpression(nameExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aTaskName, task.getName());
  }

  public void testDecorateDescription() {
    // given
    String aDescription = "This is a Task";
    Expression descriptionExpression = expressionManager.createExpression(aDescription);
    taskDefinition.setDescriptionExpression(descriptionExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aDescription, task.getDescription());
  }

  public void testDecorateDescriptionFromVariable() {
    // given
    String aDescription = "This is a Task";
    taskService.setVariable(task.getId(), "description", aDescription);

    Expression descriptionExpression = expressionManager.createExpression("${description}");
    taskDefinition.setDescriptionExpression(descriptionExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aDescription, task.getDescription());
  }

  public void testDecorateDueDate() {
    // given
    String aDueDate = "2014-06-01";
    Date dueDate = DateTimeUtil.parseDate(aDueDate);

    Expression dueDateExpression = expressionManager.createExpression(aDueDate);
    taskDefinition.setDueDateExpression(dueDateExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(dueDate, task.getDueDate());
  }

  public void testDecorateDueDateFromVariable() {
    // given
    String aDueDate = "2014-06-01";
    Date dueDate = DateTimeUtil.parseDate(aDueDate);
    taskService.setVariable(task.getId(), "dueDate", dueDate);

    Expression dueDateExpression = expressionManager.createExpression("${dueDate}");
    taskDefinition.setDueDateExpression(dueDateExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(dueDate, task.getDueDate());
  }

  public void testDecorateFollowUpDate() {
    // given
    String aFollowUpDate = "2014-06-01";
    Date followUpDate = DateTimeUtil.parseDate(aFollowUpDate);

    Expression followUpDateExpression = expressionManager.createExpression(aFollowUpDate);
    taskDefinition.setFollowUpDateExpression(followUpDateExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(followUpDate, task.getFollowUpDate());
  }

  public void testDecorateFollowUpDateFromVariable() {
    // given
    String aFollowUpDateDate = "2014-06-01";
    Date followUpDate = DateTimeUtil.parseDate(aFollowUpDateDate);
    taskService.setVariable(task.getId(), "followUpDate", followUpDate);

    Expression followUpDateExpression = expressionManager.createExpression("${followUpDate}");
    taskDefinition.setFollowUpDateExpression(followUpDateExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(followUpDate, task.getFollowUpDate());
  }

  public void testDecoratePriority() {
    // given
    String aPriority = "10";
    Expression priorityExpression = expressionManager.createExpression(aPriority);
    taskDefinition.setPriorityExpression(priorityExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(Integer.parseInt(aPriority), task.getPriority());
  }

  public void testDecoratePriorityFromVariable() {
    // given
    int aPriority = 10;
    taskService.setVariable(task.getId(), "priority", aPriority);

    Expression priorityExpression = expressionManager.createExpression("${priority}");
    taskDefinition.setPriorityExpression(priorityExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aPriority, task.getPriority());
  }

  public void testDecorateAssignee() {
    // given
    String aAssignee = "john";
    Expression assigneeExpression = expressionManager.createExpression(aAssignee);
    taskDefinition.setAssigneeExpression(assigneeExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aAssignee, task.getAssignee());
  }

  public void testDecorateAssigneeFromVariable() {
    // given
    String aAssignee = "john";
    taskService.setVariable(task.getId(), "assignee", aAssignee);

    Expression assigneeExpression = expressionManager.createExpression("${assignee}");
    taskDefinition.setAssigneeExpression(assigneeExpression);

    // when
    decorate(task, taskDecorator);

    // then
    assertEquals(aAssignee, task.getAssignee());
  }

  protected class DecorateTaskCommand implements Command<Void> {

    protected TaskEntity task;
    protected TaskDecorator decorator;

    public DecorateTaskCommand(TaskEntity task, TaskDecorator decorator) {
     this.task = task;
     this.decorator = decorator;
    }

    public Void execute(CommandContext commandContext) {
      decorator.decorate(task, task);
      return null;
    }

  }

  protected class DeleteTaskCommand implements Command<Void> {

    protected TaskEntity task;

    public DeleteTaskCommand(TaskEntity task) {
     this.task = task;
    }

    public Void execute(CommandContext commandContext) {
      commandContext
        .getTaskManager()
        .deleteTask(task, null, true, false);

      return null;
    }

  }

}
