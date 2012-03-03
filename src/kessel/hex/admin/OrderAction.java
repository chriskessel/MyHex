package kessel.hex.admin;

import javax.swing.*;
import java.awt.Graphics;

/** Base class for all Actions handled in the Admin/Player displays. */
public abstract class OrderAction extends AbstractAction
{
  protected OrderAction( String action )
  {
    super( action );
  }

  /** If the action has any onscreen info it needs to paint while the action is executing, this method handles it. */
  public void paint( Graphics g )
  {}
}
