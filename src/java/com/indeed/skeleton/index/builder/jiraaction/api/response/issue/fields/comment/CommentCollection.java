package com.indeed.skeleton.index.builder.jiraaction.api.response.issue.fields.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by soono on 8/25/16.
 */

@JsonIgnoreProperties(ignoreUnknown=true)

public class CommentCollection {
    public Comment[] comments;

    public void sortComments() throws ParseException {
        // It seems JIRA API's response is already sorted, but
        // just in case, use this method to make sure.
        // Because it's usually already sorted, use insertion sort algorithm here.

        for (int i=1; i<comments.length; i++) {
            final Comment comment = comments[i];
            final Date date = parseDate(comment.created);
            for (int k=i-1; k>=0; k--) {
                final Comment comparedComment = comments[k];
                final Date comparedDate = parseDate(comparedComment.created);
                if (date.after(comparedDate)) {
                    comments[k+1] = comment;
                    break;
                }
                else {
                    comments[k+1] = comments[k];
                }
            }
        }
    }

    private Date parseDate(final String dateString) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final String strippedCreatedString = dateString.replace('T', ' ');
        final Date date = dateFormat.parse(strippedCreatedString);
        return date;
    }
}
