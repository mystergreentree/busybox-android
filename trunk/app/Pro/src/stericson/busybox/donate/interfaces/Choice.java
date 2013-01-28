package stericson.busybox.donate.interfaces;

public interface Choice
{
	public void choiceMade(boolean choice, int id);
	public void choiceCancelled(int id);
}
