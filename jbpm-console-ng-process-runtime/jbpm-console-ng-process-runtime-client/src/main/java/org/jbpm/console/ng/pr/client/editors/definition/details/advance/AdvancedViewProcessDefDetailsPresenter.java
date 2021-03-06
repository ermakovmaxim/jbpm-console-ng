/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.console.ng.pr.client.editors.definition.details.advance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.bd.service.DataServiceEntryPoint;
import org.jbpm.console.ng.ga.model.process.DummyProcessPath;
import org.jbpm.console.ng.ht.model.TaskDefSummary;
import org.jbpm.console.ng.pr.client.editors.definition.details.BaseProcessDefDetailsPresenter;
import org.jbpm.console.ng.pr.client.i18n.Constants;
import org.jbpm.console.ng.pr.model.ProcessDefinitionKey;
import org.jbpm.console.ng.pr.model.ProcessSummary;
import org.jbpm.console.ng.pr.service.ProcessDefinitionService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class AdvancedViewProcessDefDetailsPresenter extends
        BaseProcessDefDetailsPresenter {

    public interface AdvancedProcessDefDetailsView extends
            BaseProcessDefDetailsPresenter.BaseProcessDefDetailsView {

        HTML getNroOfHumanTasksText();

        HTML getHumanTasksListBox();

        HTML getUsersGroupsListBox();

        HTML getProcessDataListBox();

        HTML getProcessServicesListBox();

        HTML getSubprocessListBox();
    }

    private Constants constants = Constants.INSTANCE;

    @Inject
    private AdvancedProcessDefDetailsView view;

    @Inject
    private Caller<DataServiceEntryPoint> dataServices;

    @Inject
    private Caller<ProcessDefinitionService> processDefService;

    @Inject
    private Caller<VFSService> fileServices;

    @Override
    public IsWidget getWidget() {
        return view;
    }

    @Override
    protected void refreshView( String processId, String deploymentId ) {
        view.getProcessIdText().setText( processId );
        view.getDeploymentIdText().setText( deploymentId );
    }

    private void refreshServiceTasks(final String deploymentId, final String processId) {
        dataServices.call(
                new RemoteCallback<Map<String, String>>() {
                    @Override
                    public void callback(Map<String, String> services) {
                        view.getProcessServicesListBox().setText("");
                        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                        if (services.keySet().isEmpty()) {
                            safeHtmlBuilder.appendEscaped(constants.NoServicesRequiredForThisProcess());
                            view.getProcessServicesListBox().setStyleName("muted");
                            view.getProcessServicesListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        } else {
                            for (String key : services.keySet()) {
                                safeHtmlBuilder.appendEscapedLines(key + " - "
                                        + services.get(key) + "\n");
                            }
                            view.getProcessServicesListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getServiceTasks(deploymentId, processId);
    }

    private void refreshProcessItems(final String deploymentId,
            final String processId) {
        processDefService.call(
                new RemoteCallback<ProcessSummary>() {
                    @Override
                    public void callback(ProcessSummary process) {
                        if (process != null) {
                            view.setEncodedProcessSource(process
                                    .getEncodedProcessSource());
                            view.getProcessNameText().setText(process.getName());
                            if (process.getOriginalPath() != null) {
                                fileServices.call(new RemoteCallback<Path>() {

                                    @Override
                                    public void callback(Path processPath) {
                                        view.setProcessAssetPath(processPath);
                                    }
                                }).get(process.getOriginalPath());
                            } else {
                                view.setProcessAssetPath(new DummyProcessPath(process.getProcessDefId()));
                            }
                            changeStyleRow(process.getName(), process.getVersion());
                        } else {
                            // set to null to ensure it's clear state
                            view.setEncodedProcessSource(null);
                            view.setProcessAssetPath(null);
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getItem(new ProcessDefinitionKey(deploymentId, processId));
    }

    private void refreshReusableSubProcesses(final String deploymentId, final String processId) {
        dataServices.call(
                new RemoteCallback<Collection<String>>() {
                    @Override
                    public void callback(Collection<String> subprocesses) {
                        view.getSubprocessListBox().setText("");
                        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                        if (subprocesses.isEmpty()) {
                            safeHtmlBuilder.appendEscapedLines(constants.NoSubprocessesRequiredByThisProcess());
                            view.getSubprocessListBox().setStyleName("muted");
                            view.getSubprocessListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        } else {
                            for (String key : subprocesses) {
                                safeHtmlBuilder.appendEscapedLines(key + "\n");
                            }
                            view.getSubprocessListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getReusableSubProcesses(deploymentId, processId);
    }

    private void refreshRequiredInputData(final String deploymentId, final String processId) {
        dataServices.call(
                new RemoteCallback<Map<String, String>>() {
                    @Override
                    public void callback(Map<String, String> inputs) {
                        view.getProcessDataListBox().setText("");
                        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                        if (inputs.keySet().isEmpty()) {
                            safeHtmlBuilder.appendEscapedLines(constants.NoProcessVariablesDefinedForThisProcess());
                            view.getProcessDataListBox().setStyleName("muted");
                            view.getProcessDataListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        } else {
                            for (String key : inputs.keySet()) {
                                safeHtmlBuilder.appendEscapedLines(key + " - "
                                        + inputs.get(key) + "\n");
                            }
                            view.getProcessDataListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getRequiredInputData(deploymentId, processId);
    }

    private void refreshAssociatedEntities(final String deploymentId, final String processId) {
        dataServices.call(
                new RemoteCallback<Map<String, Collection<String>>>() {
                    @Override
                    public void callback(Map<String, Collection<String>> entities) {
                        view.getUsersGroupsListBox().setText("");
                        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                        if (entities.keySet().isEmpty()) {
                            safeHtmlBuilder
                            .appendEscapedLines(constants.NoUserOrGroupUsedInThisProcess());
                            view.getUsersGroupsListBox().setStyleName("muted");
                            view.getUsersGroupsListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        } else {
                            for (String key : entities.keySet()) {
                                StringBuffer names = new StringBuffer();
                                Collection<String> entityNames = entities
                                .get(key);
                                if (entityNames != null) {
                                    for (String entity : entityNames) {
                                        names.append("'" + entity + "' ");
                                    }
                                }
                                safeHtmlBuilder.appendEscapedLines(names
                                        + " - " + key + "\n");
                            }
                            view.getUsersGroupsListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getAssociatedEntities(deploymentId, processId);
    }

    private void refreshTaskDef(final String deploymentId, final String processId) {
        dataServices.call(
                new RemoteCallback<List<TaskDefSummary>>() {

                    @Override
                    public void callback(List<TaskDefSummary> tasks) {
                        view.getNroOfHumanTasksText().setText(
                                String.valueOf(tasks.size()));
                        view.getHumanTasksListBox().setText("");
                        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                        if (tasks.isEmpty()) {
                            safeHtmlBuilder.appendEscapedLines(constants.NoUserTasksDefinedInThisProcess());
                            view.getHumanTasksListBox().setStyleName("muted");
                            view.getHumanTasksListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        } else {
                            for (TaskDefSummary t : tasks) {
                                safeHtmlBuilder.appendEscapedLines(t.getName() + "\n");
                            }
                            view.getHumanTasksListBox().setHTML(
                                    safeHtmlBuilder.toSafeHtml());
                        }
                    }
                },
                new DefaultErrorCallback()
        ).getAllTasksDef(deploymentId, processId);
    }

    @Override
    protected void refreshProcessDef( final String deploymentId, final String processId ) {
        refreshTaskDef( deploymentId, processId );

        refreshAssociatedEntities( deploymentId, processId );

        refreshRequiredInputData( deploymentId, processId );

        refreshReusableSubProcesses( deploymentId, processId );

        refreshProcessItems( deploymentId, processId );

        refreshServiceTasks( deploymentId, processId );
    }
}
