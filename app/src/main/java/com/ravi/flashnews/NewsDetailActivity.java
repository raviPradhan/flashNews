package com.ravi.flashnews;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.JsonKeys;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDetailActivity extends AppCompatActivity {

    @BindView(R.id.iv_detail_image)
    ImageView image;
    @BindView(R.id.tv_detail_content)
    TextView content;
    @BindView(R.id.tv_detail_title)
    TextView title;
    @BindView(R.id.tv_detail_date)
    TextView publishedDate;
    @BindView(R.id.tv_detail_source)
    TextView source;
    @BindView(R.id.tv_detail_url)
    TextView fullStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);
        supportPostponeEnterTransition();

        Bundle extras = getIntent().getExtras();
        final News newsItem = extras.getParcelable(JsonKeys.EXTRA_NEWS_ITEM);
        title.setText(newsItem.getTitle());
        content.setText(newsItem.getDescription());
        source.setText(getString(R.string.author, newsItem.getSourceName()));
        publishedDate.setText(newsItem.getPublishedDate());

        String imageUrl = newsItem.getImageUrl();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString(JsonKeys.EXTRA_NEWS_IMAGE_TRANSITION_NAME);
            image.setTransitionName(imageTransitionName);
        }
        Picasso.get()
                .load(imageUrl)
                .noFade()
                .into(image, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                    }
                });
        Spannable wordtoSpan = new SpannableString(getString(R.string.read_full));
        wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorIcons)),
                0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        fullStory.setText(wordtoSpan);
        fullStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(newsItem.getFullUrl()));
                startActivity(intent);
            }
        });
    }
}
