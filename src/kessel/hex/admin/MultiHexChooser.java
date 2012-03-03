package kessel.hex.admin;

import kessel.hex.util.Tuple;

import java.awt.event.MouseEvent;

/**
 * Listens for mouse events to select multiple hexes. Detects the user is done by a double click. If the user clicks on a hex already
 * selected, it'll deselect it.
 */
class MultiHexChooser extends TargetListener
{
  /** The action that caused us to go looking for a target. */
  private final ArmyMoveActions.AbstractArmyMoveAction _action;

  private final AbstractGameDisplay _gameDisplay;

  MultiHexChooser( AbstractGameDisplay gameDisplay, ArmyMoveActions.AbstractArmyMoveAction action )
  {
    _action = action;
    _gameDisplay = gameDisplay;
  }

  public void mouseClicked( MouseEvent e )
  {
    if ( _action.getTargetState() == TargetAction.TargetState.SELECTING )
    {
      Tuple hex = _gameDisplay.getCellMetrics().getGridByPixel( e.getX(), e.getY() );
      if ( _gameDisplay.isOnMap( hex ) )
      {
        if ( e.getButton() == MouseEvent.BUTTON1 )
        {
          _action.hexSelected( hex );
        }
        else if ( e.getButton() == MouseEvent.BUTTON3 )
        {
          _action.selectionFinished();
        }
      }
    }
  }
}
