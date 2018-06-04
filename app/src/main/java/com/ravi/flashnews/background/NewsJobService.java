package com.ravi.flashnews.background;

import android.content.Context;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.ravi.flashnews.loaders.BackgroundAsyncTask;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.NotificationUtils;

public class NewsJobService extends JobService {

    private BackgroundAsyncTask mBackgroundTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        // Here's where we make an AsyncTask so that this is no longer on the main thread
        mBackgroundTask = new BackgroundAsyncTask(new JobCallback() {
            @Override
            public void resultCallback(News news) {
                Context context = NewsJobService.this;
                // Prepare to show notification
                NotificationUtils.generateNotificationLayout(context, news);
                //Override onPostExecute and called jobFinished. Pass the job parameters
                // and false to jobFinished. This will inform the JobManager that your job is done
                // and that you do not want to reschedule the job.

                /*
                 * Once the AsyncTask is finished, the job is finished. To inform JobManager that
                 * you're done, you call jobFinished with the jobParamters that were passed to your
                 * job and a boolean representing whether the job needs to be rescheduled. This is
                 * usually if something didn't work and you want the job to try running again.
                 */
                jobFinished(jobParameters, false);
            }
        });
        // Execute the AsyncTask
        mBackgroundTask.execute();
        return true;
    }

    public interface JobCallback {
        void resultCallback(News news);
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     */
    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        // If mBackgroundTask is valid, cancel it
        //Return true to signify the job should be retried
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
