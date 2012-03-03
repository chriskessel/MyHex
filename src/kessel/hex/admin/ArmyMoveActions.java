package kessel.hex.admin;

import kessel.hex.domain.Army;
import kessel.hex.domain.GameItem;
import kessel.hex.map.Terrain;
import kessel.hex.orders.army.AbstractArmyMove;
import kessel.hex.orders.army.ArmyMove;
import kessel.hex.orders.army.ArmySearch;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/** A static holder for army movement actions. */
public class ArmyMoveActions
{
  /** @return the order or null if the order type isn't right for an implied order. */
  public static Action createAction( PlayerDetailsPane playerDetailsPane, Class orderType, GameItem item )
  {
    if ( orderType.equals( ArmyMove.class ) )
    {
      return new ArmyMoveAction( playerDetailsPane, (Army) item );
    }
    if ( orderType.equals( ArmySearch.class ) )
    {
      return new ArmySearchAction( playerDetailsPane, (Army) item );
    }
    else
    {
      return null;
    }
  }

  abstract static class AbstractArmyMoveAction extends OrderAction implements TargetAction
  {
    protected final Army _subject;
    protected List<Tuple> _moves = new ArrayList<>();
    protected final PlayerDetailsPane _playerDetailsPane;
    protected TargetAction.TargetState _targetState = TargetAction.TargetState.INITIAL;
    protected MultiHexChooser _hexChooser;

    protected AbstractArmyMoveAction( PlayerDetailsPane playerDetailsPane, Army subject, String name )
    {
      super( name );
      _subject = subject;
      this._playerDetailsPane = playerDetailsPane;
    }

    public void actionPerformed( ActionEvent e )
    {
      switch ( _targetState )
      {
        case INITIAL:
          _playerDetailsPane.setActionInProgress( this );
          _hexChooser = new MultiHexChooser( _playerDetailsPane._gameDisplay, this );
          switchToTargetCursor();
          _targetState = TargetState.SELECTING;
          break;
        case SELECTING:
          break;
      }
    }

    protected void switchToTargetCursor()
    {
      _playerDetailsPane._gameDisplay.setCursor( new Cursor( Cursor.CROSSHAIR_CURSOR ) );
    }

    protected void switchToNormalCursor()
    {
      _playerDetailsPane._gameDisplay.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void cancelTargetAcquisition()
    {
      _playerDetailsPane.setActionInProgress( null );
      _targetState = TargetState.INITIAL;
      switchToNormalCursor();
    }

    public void hexSelected( Tuple nextHex )
    {
      Tuple lastHex = _moves.isEmpty() ? _subject.getLocation().getCoord() : _moves.get( _moves.size() - 1 );
      if ( _moves.contains( nextHex ) )
      {
        if ( nextHex.equals( lastHex ) )
        {
          _moves.remove( nextHex );
        }
        else
        {
          JOptionPane.showMessageDialog( _playerDetailsPane._gameDisplay, "Can only cancel the previous movement hex." );
        }
      }
      else
      {
        boolean isValidNextStep = HexCalculator.isAdjacent( lastHex, nextHex );
        if ( isValidNextStep )
        {
          _moves.add( nextHex );
          int cost = calculateMovementCosts( _moves );
          if ( cost > ArmyMove.MAX_MOVEMENT )
          {
            JOptionPane.showMessageDialog(
              _playerDetailsPane._gameDisplay, "That hex would exceed our movement ability." );
            _moves.remove( nextHex );
          }
        }
        else
        {
          JOptionPane.showMessageDialog(
            _playerDetailsPane._gameDisplay, "That hex is not adjacent to the previous movement hex." );
        }
      }
      _playerDetailsPane._gameDisplay.repaint();
    }

    private int calculateMovementCosts( List<Tuple> moves )
    {
      int cost = 0;
      for ( Tuple move : moves )
      {
        Terrain terrain = _playerDetailsPane._gameDisplay.getLocationAtHex( move ).getTerrain();
        cost += _playerDetailsPane.getPlayer().getKingdom().getMovementCost( terrain );
      }
      return cost;
    }

    public void selectionFinished()
    {
      _targetState = TargetState.INITIAL;
      _playerDetailsPane.setActionInProgress( null );
      switchToNormalCursor();

      if ( !_moves.isEmpty() )
      {
        AbstractArmyMove order = createOrder();
        _subject.addNextTurnOrder( order );
        _playerDetailsPane.updateOrders( order );
        _playerDetailsPane._gameDisplay.repaint();
        _playerDetailsPane.updateDetails();
      }
    }

    protected abstract AbstractArmyMove createOrder();

    public void paint( Graphics g )
    {
      PaintOrderUtil.paintArmyMove( g, _playerDetailsPane, _moves );
    }

    public TargetListener getTargetChooser() { return _hexChooser; }

    public TargetState getTargetState() { return _targetState; }

    public boolean isQuickTarget() { return true; }
  }

  static class ArmyMoveAction extends AbstractArmyMoveAction
  {
    protected ArmyMoveAction( PlayerDetailsPane playerDetailsPane, Army subject )
    {
      super( playerDetailsPane, subject, "Move" );
    }

    protected ArmyMove createOrder()
    {
      return new ArmyMove( _subject, _moves );
    }
  }

  static class ArmySearchAction extends AbstractArmyMoveAction
  {
    protected ArmySearchAction( PlayerDetailsPane playerDetailsPane, Army subject )
    {
      super( playerDetailsPane, subject, "Search" );
    }

    protected ArmySearch createOrder()
    {
      return new ArmySearch( _subject, _moves );
    }
  }
}