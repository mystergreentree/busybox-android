package stericson.busybox.donate.interfaces;

public interface ChoiceCallback {
    public void choiceMade(boolean choice, int id);

    public void choiceCancelled(int id);
}
