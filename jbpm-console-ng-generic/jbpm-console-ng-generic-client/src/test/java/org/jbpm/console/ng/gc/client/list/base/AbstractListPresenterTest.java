/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.jbpm.console.ng.gc.client.list.base;


import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.jbpm.console.ng.gc.client.list.base.events.SearchEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(GwtMockitoTestRunner.class)
public class AbstractListPresenterTest {

    @Mock
    private Timer timer;

    @Mock
    private AbstractListView.ListView viewMock;

    @Mock
    HasData next;

    private AbstractListPresenter testListPresenter;

    @Before
    public void setupMocks() {
        testListPresenter = spy(AbstractScreenListPresenter.class);
        when(testListPresenter.getListView()).thenReturn(viewMock);
        when(next.getVisibleRange()).thenReturn(new Range(1, 1));
        testListPresenter.initDataProvider();
        testListPresenter.getDataProvider().addDataDisplay(next);
    }

    @Test
    public void autoRefreshDisabledByDefaultTest() {
        testListPresenter.setRefreshTimer(null);
        testListPresenter.updateRefreshTimer();

        assertNotNull(testListPresenter.getRefreshTimer());
        assertFalse(testListPresenter.isAutoRefreshEnabled());

        testListPresenter.setRefreshTimer(timer);
        testListPresenter.setAutoRefreshSeconds(60);
        testListPresenter.updateRefreshTimer();

        assertFalse(testListPresenter.isAutoRefreshEnabled());
        verify(timer).cancel();
    }

    @Test
    public void autoRefreshEnabledScheduleTimerTest() {
        testListPresenter.setAutoRefreshEnabled(true);
        testListPresenter.setAutoRefreshSeconds(60);
        testListPresenter.setRefreshTimer(timer);
        testListPresenter.updateRefreshTimer();

        assertNotNull(testListPresenter.getRefreshTimer());
        verify(timer).cancel();
        verify(timer).schedule(60000);
    }


    @Test
    public void restoreTabsTest() {
        testListPresenter.onRestoreDefaultFilters();

        verify(viewMock).showRestoreDefaultFilterConfirmationPopup();
    }

    @Test
    public void testEmptySearchString() {
        final SearchEvent searchEvent = new SearchEvent("");

        testListPresenter.onSearchEvent(searchEvent);

        final ArgumentCaptor<Range> captor = ArgumentCaptor.forClass(Range.class);
        verify(next).setVisibleRangeAndClearData(captor.capture(), eq(true));
        final Range range = captor.getValue();
        assertEquals(1, range.getStart());
        assertEquals(1, range.getLength());
        assertEquals(searchEvent.getFilter(), testListPresenter.getTextSearchStr());
    }

    @Test
    public void testSearchString() {
        final SearchEvent searchEvent = new SearchEvent(RandomStringUtils.random(10));

        testListPresenter.onSearchEvent(searchEvent);

        final ArgumentCaptor<Range> captor = ArgumentCaptor.forClass(Range.class);
        verify(next).setVisibleRangeAndClearData(captor.capture(), eq(true));
        final Range range = captor.getValue();
        assertEquals(0, range.getStart());
        assertEquals(1, range.getLength());
        assertEquals(searchEvent.getFilter().toLowerCase(), testListPresenter.getTextSearchStr());
    }
}