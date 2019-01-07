package iu9.bmstu.ru.lab10;

public final class Const {
    private Const() {}

    public static final String NOTIFICATION_PLAYER_CHANNEL_ID = "iu9.bmstu.ru.lab10.channel.player";
    public static final int PLAYER_NOTIFICATION_ID = 5;

    public interface Action {
        String START_SVC = "iu9.bmstu.ru.lab10.action.start_svc";
        String PLAY = "iu9.bmstu.ru.lab10.action.play";
        String STOP = "iu9.bmstu.ru.lab10.action.stop";
        String STOP_SVC = "iu9.bmstu.ru.lab10.action.stop_svc";
    }

}
