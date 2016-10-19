package com.indeed.skeleton.index.builder.jiraaction;

import com.indeed.common.cli.CommandLineTool;
import com.indeed.common.cli.CommandLineUtil;
import com.indeed.common.dbutil.CronToolStatusUpdater;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * @author soono
 * @author kbinswanger
 */
public class JiraActionIndexBuilderCommandLineTool implements CommandLineTool {
    private static final Logger log = Logger.getLogger(JiraActionIndexBuilderCommandLineTool.class);

    private JiraActionIndexBuilder indexBuilder;

    public static void main(final String[] args) {
        final CommandLineUtil cmdLineUtil = new CommandLineUtil(log, args, new JiraActionIndexBuilderCommandLineTool());
        cmdLineUtil.addStatusUpdateFunction(new CronToolStatusUpdater(
                JiraActionIndexBuilderCommandLineTool.class.getName(),
                JiraActionIndexBuilderCommandLineTool.class.getSimpleName(),
                cmdLineUtil.getArgs(), true));
        cmdLineUtil.run();
    }

    @Override
    public void initialize(final CommandLineUtil cmdLineUtil) {
        final Configuration config = cmdLineUtil.getProperties();
        final String jiraUsername = config.getString("jira.username.indexer");
        final String jiraPassword = config.getString("jira.password.indexer");

        final String jiraBaseUrl = config.getString("jira.baseurl");
        final String[] jiraFieldArray = config.getStringArray("jira.fields");
        final String jiraExpand = config.getString("jira.expand");
        final String jiraProject = config.getString("jira.project");
        final String iuploadUrl = config.getString("iupload.url");

        final StringBuilder jiraFields = new StringBuilder();
        for(final String field : jiraFieldArray) {
            if(jiraFields.length() > 0) {
                jiraFields.append(",");
            }
            jiraFields.append(field);
        }

        final JiraActionIndexBuilderConfig indexBuilderConfig = new JiraActionIndexBuilderConfig(jiraUsername,
                jiraPassword, jiraBaseUrl, jiraFields.toString(), jiraExpand, jiraProject, iuploadUrl);
        indexBuilder = new JiraActionIndexBuilder(indexBuilderConfig);
    }

    @Override
    public void run(final CommandLineUtil cmdLineUtil) {
        indexBuilder.run();
    }
}