package com.indeed.jiraactions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.indeed.jiraactions.api.IssueAPIParser;
import com.indeed.jiraactions.api.customfields.CustomFieldApiParser;
import com.indeed.jiraactions.api.customfields.CustomFieldDefinition;
import com.indeed.jiraactions.api.response.issue.Issue;
import com.indeed.jiraactions.api.response.issue.changelog.histories.History;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class ActionFactoryTest {
    private final UserLookupService userLookupService = new FriendlyUserLookupService();

    @Test
    public void testCreation() throws IOException {
        ActionFactory factory = newActionFactory();

        try (final InputStream stream = getClass().getResourceAsStream("/ENGPLANS-10.json")) {
            Assert.assertNotNull(stream);
            final JsonNode node = new ObjectMapper().readTree(stream);
            Issue issue = IssueAPIParser.getObject(node);
            final Action action = factory.create(issue);

            Assert.assertEquals("2009-02-12", action.getCreatedDate());
            Assert.assertEquals(20090212, action.getCreatedDateLong());
            Assert.assertEquals(20090212174027L, action.getCreatedDateTimeLong());
            Assert.assertEquals(1234482027, action.getCreatedDateTimestamp());

            Assert.assertEquals("2009-02-12", action.getResolutionDate());
            Assert.assertEquals(20090212, action.getResolutionDateLong());
            Assert.assertEquals(20090212184850L, action.getResolutionDateTimeLong());
            Assert.assertEquals(1234486130, action.getResolutionDateTimestamp());
        }
    }

    @Test
    /**
     * Components that are added to an issue when the issue is first created will never appear in
     *  the changelog. Make sure that we are adding this to the issue.
     */
    public void testComponentsCreatedWithIssue() throws IOException {
        ActionFactory factory = newActionFactory();

        try (final InputStream stream = getClass().getResourceAsStream("/ENGPLANS-10.json")) {
            Assert.assertNotNull(stream);
            final JsonNode node = new ObjectMapper().readTree(stream);
            Issue issue = IssueAPIParser.getObject(node);
            final Action action = factory.create(issue);

            Assert.assertThat(action.getComponents(), is(equalTo(ImmutableList.of("Eng"))));
        }
    }

    /**
     * Fix Versions that are added to an issue when the issue is first created will never appear in
     *  the changelog. Make sure that we are adding this to the issue.
     */
    @Test
    public void testFixVersionsCreatedWithIssueAndAddedLater() throws IOException {
        ActionFactory factory = newActionFactory();

        try (final InputStream stream = getClass().getResourceAsStream("/ENGPLANS-10.testMultipleFixVersionsMixedHistory.json")) {
            Assert.assertNotNull(stream);
            final JsonNode node = new ObjectMapper().readTree(stream);
            Issue issue = IssueAPIParser.getObject(node);
            Action action = factory.create(issue);

            Assert.assertThat(
                    "Expected the initial value of the fixVersions to include the value added on creation and remove the value added in history.",
                    action.getFixVersions(), is(equalTo(ImmutableList.of("Private launch"))));

            for (final History history: issue.changelog.histories) {
                action = factory.update(action, history);
            }

            Assert.assertThat(
                    "Expected the initial value of the fixVersions to include the value added on creation and remove the value added in history.",
                    action.getFixVersions(), is(equalTo(ImmutableList.of("Private launch", "Public launch"))));

            Assert.assertThat(
                    "Expected the initial value of the fixVersions to include the value added on creation and remove the value added in history.",
                    action.getFixVersionsJoined(), is(equalTo("Private launch|Public launch")));

        }
    }

    private ActionFactory newActionFactory() {
        final JiraActionsIndexBuilderConfig config = EasyMock.createNiceMock(JiraActionsIndexBuilderConfig.class);
        EasyMock.expect(config.getCustomFields()).andReturn(new CustomFieldDefinition[0]).anyTimes();
        EasyMock.replay(config);

        return new ActionFactory(userLookupService, new CustomFieldApiParser(userLookupService), config);
    }
}
