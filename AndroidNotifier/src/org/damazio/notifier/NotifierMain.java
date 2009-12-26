package org.damazio.notifier;

import org.damazio.notifier.notification.Notification;
import org.damazio.notifier.notification.NotificationType;
import org.damazio.notifier.notification.Notifier;
import org.damazio.notifier.service.NotificationService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NotifierMain extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	NotificationService.start(this);

	Button testButton = (Button) findViewById(R.id.test_notification);
	testButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Notifier notifier = new Notifier(NotifierMain.this);
        Notification notification = new Notification(NotifierMain.this, NotificationType.PING, "test");
        notifier.sendNotification(notification);
      }
    });
  }
}