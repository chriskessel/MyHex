package kessel.hex.admin;

/** For all actions that require target acquisition. */
interface TargetAction
{
  enum TargetState
  {
    INITIAL, SELECTING
  }

  /** Cancels the target selection process. */
  void cancelTargetAcquisition();

  /** Get the listener for the actions associated with the target selection. */
  TargetListener getTargetChooser();

  TargetState getTargetState();

  /** @return true if it's a quick (single click) target action. */
  boolean isQuickTarget();
}
