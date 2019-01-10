package iu9.bmstu.ru.lab10;

public final class Const {
    private Const() {}

    public static final String NOTIFICATION_PLAYER_CHANNEL_ID = "iu9.bmstu.ru.lab10.channel.player";
    public static final String AIRPLANE_MODE_CHANNEL_ID = "iu9.bmstu.ru.lab10.channel.media_button";
    public static final int PLAYER_NOTIFICATION_ID = 5;
    public static final int AIRPLANE_MODE_NOTIFICATION_ID = 6;

    public interface Action {
        String START_SVC = "iu9.bmstu.ru.lab10.action.start_svc";
        String PLAYBACK_START = "iu9.bmstu.ru.lab10.action.play";
        String PLAYBACK_STOP = "iu9.bmstu.ru.lab10.action.stop";
        String STOP_SVC = "iu9.bmstu.ru.lab10.action.stop_svc";
    }

}
