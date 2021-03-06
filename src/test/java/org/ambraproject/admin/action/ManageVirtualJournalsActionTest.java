/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Volume;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 1/30/12
 */
public class ManageVirtualJournalsActionTest extends AdminWebTest {

  @Autowired
  protected ManageVirtualJournalsAction action;

  @DataProvider(name = "basicInfo")
  public Object[][] getCurrentIssueAndVolumes() {
    Journal journal = new Journal();
    journal.setJournalKey("journalForTestManageJournals");
    journal.seteIssn("fakeEIssn");
    journal.setVolumes(new ArrayList<Volume>(3));

    Issue currentIssue = new Issue();
    currentIssue.setIssueUri("id:current-issue-for-test-volume");
    dummyDataStore.store(currentIssue);
    journal.setCurrentIssue(dummyDataStore.get(Issue.class,currentIssue.getID()));

    for (int i = 1; i <= 3; i++) {
      Volume volume = new Volume();
      volume.setDisplayName("200" + i);
      volume.setVolumeUri("id:fake-volume-for-manage-journals" + i);
      dummyDataStore.store(volume);
      journal.getVolumes().add(dummyDataStore.get(Volume.class,volume.getID()));
    }

    dummyDataStore.store(journal);

    return new Object[][]{
        {journal, currentIssue.getIssueUri()}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(Journal journal, String currentIssue) throws Exception {
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default execute");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    assertEquals(action.getJournal().getCurrentIssue().getIssueUri(), currentIssue, "action didn't get correct issue");
    assertEquals(action.getVolumes().size(), journal.getVolumes().size(), "Action returned incorrect number of volumes");
    for (int i = 0; i < journal.getVolumes().size(); i++) {
      Volume actual = action.getVolumes().get(i);
      Volume expected = journal.getVolumes().get(i);
      assertEquals(actual.getVolumeUri(), expected.getVolumeUri(), "Volume " + (i + 1) + " didn't have correct uri");
      assertEquals(actual.getDisplayName(), expected.getDisplayName(),
          "Volume " + (i + 1) + " didn't have correct display name");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateVolume(Journal journal, String currentIssue) throws Exception {
    int initialNumberOfVolumes = dummyDataStore.get(Journal.class, journal.getID()).getVolumes().size();
    String volumeUri = "id:new-volume-for-create-volume";
    String volumeDisplayName = "That Still Small Voice";
    //set properties on the action
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("CREATE_VOLUME");
    action.setVolumeURI(volumeUri);
    action.setDisplayName(volumeDisplayName);

    //run the action
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check action's return values
    assertEquals(action.getVolumes().size(), initialNumberOfVolumes + 1, "action didn't add new volume to list");
    Volume actualVolume = action.getVolumes().get(action.getVolumes().size() - 1);
    assertEquals(actualVolume.getVolumeUri(), volumeUri, "Volume didn't have correct uri");
    assertEquals(actualVolume.getDisplayName(), volumeDisplayName, "Volume didn't have correct id");

    assertTrue(action.getActionMessages().size() > 0, "Action didn't return a message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check values stored to the database
    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getID());
    assertEquals(storedJournal.getVolumes().size(), initialNumberOfVolumes + 1,
        "journal didn't get volume added in the database");

    assertEquals(storedJournal.getVolumes().get(storedJournal.getVolumes().size() - 1).getVolumeUri(), volumeUri,
        "Journal didn't have volume added in the db");

    //try creating a duplicate volume and see if we get an error message
    action.execute();
    assertEquals(action.getActionErrors().size(), 1, "action didn't add error when trying to save duplicate volume");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveVolumes(Journal journal, String currentIssue) throws Exception {
    List<Volume> initialVolumes = dummyDataStore.get(Journal.class, journal.getID()).getVolumes();

    String[] urisToDelte = new String[]{initialVolumes.get(0).getVolumeUri(), initialVolumes.get(2).getVolumeUri()};
    List<Volume> volumesToDelete = new ArrayList<Volume>(urisToDelte.length);
    for (Volume volume : initialVolumes) {
      if (ArrayUtils.indexOf(urisToDelte, volume.getVolumeUri()) != -1) {
        volumesToDelete.add(volume);
      }
    }

    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("REMOVE_VOLUMES");
    action.setVolsToDelete(urisToDelte);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check the return values on the action
    assertEquals(action.getVolumes().size(), initialVolumes.size() - 2, "action didn't remove volumes");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't add message for deleting volumes");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    List<Volume> storedVolumes = dummyDataStore.get(Journal.class, journal.getID()).getVolumes();
    for (Volume deletedVolume : volumesToDelete) {
      assertFalse(storedVolumes.contains(deletedVolume), "Volume " + deletedVolume + " didn't get removed from journal");
      assertNull(dummyDataStore.get(Volume.class, deletedVolume.getID()), "Volume didn't get removed from the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testSetCurrentIssue(Journal journal, String ignored) throws Exception {
    Issue currentIssue = new Issue("id:new-issue-uri-to-set");
    dummyDataStore.store(currentIssue);

    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("UPDATE_ISSUE");
    action.setCurrentIssueURI(currentIssue.getIssueUri());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertTrue(action.getActionErrors().size() == 0, "action returned error messages");
    assertTrue(action.getActionMessages().size() > 0, "action didn't return a message indicating success");

    assertEquals(action.getJournal().getCurrentIssue().getIssueUri(), currentIssue.getIssueUri(),
        "action didn't have correct issue uri");

    String storedIssueUri = dummyDataStore.get(Journal.class, journal.getID()).getCurrentIssue().getIssueUri();

    assertEquals(storedIssueUri, currentIssue.getIssueUri(), "issue uri didn't get stored to the database");

  }


  private VirtualJournalContext makeVirtualJournalContext(Journal journal) {
    return new VirtualJournalContext(
        journal.getJournalKey(),
        "dfltJournal",
        "http",
        80,
        "localhost",
        "ambra-webapp",
        new ArrayList<String>());
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
