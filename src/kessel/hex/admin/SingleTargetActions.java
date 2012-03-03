package kessel.hex.admin;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.CombatCapableItem;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.Order;
import kessel.hex.orders.agent.AgentReconLocation;
import kessel.hex.orders.agent.AgentScoutControlLevel;
import kessel.hex.orders.agent.AgentScoutEmbassies;
import kessel.hex.orders.agent.AssassinateAgent;
import kessel.hex.orders.agent.AssassinateDiplomat;
import kessel.hex.orders.agent.AssassinateWizard;
import kessel.hex.orders.agent.MoveAgent;
import kessel.hex.orders.agent.SabotageEmbassy;
import kessel.hex.orders.agent.SabotagePopCenter;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyAttackArmy;
import kessel.hex.orders.army.ArmyAttackPop;
import kessel.hex.orders.diplomat.MapRegion;
import kessel.hex.orders.diplomat.MoveDiplomat;
import kessel.hex.orders.diplomat.TakeRegionCensus;
import kessel.hex.orders.king.ImproveEmbassy;
import kessel.hex.orders.wizard.MoveWizard;
import kessel.hex.orders.wizard.Scry;
import kessel.hex.orders.wizard.TeleportSelf;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A static holder for all the orders that have one target (e.g. moving an agent, attacking a town, recon a location, etc). */
public class SingleTargetActions
{
  /** @return the implied order or null if the order type isn't right for an implied order. */
  public static Action createAction( PlayerDetailsPane playerDetailsPane, Class orderType, GameItem item )
  {
    if ( orderType.equals( AgentReconLocation.class ) )
    {
      return new AgentReconLocationAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( AgentScoutEmbassies.class ) )
    {
      return new AgentScoutEmbassiesAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( AgentScoutControlLevel.class ) )
    {
      return new AgentScoutControlLevelAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( SabotagePopCenter.class ) )
    {
      return new AgentSabotagePopCenterAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( MoveAgent.class ) )
    {
      return new MoveAgentAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( MoveDiplomat.class ) )
    {
      return new MoveDiplomatAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( MoveWizard.class ) )
    {
      return new MoveWizardAction( playerDetailsPane, (Wizard) item );
    }
    else if ( orderType.equals( AssassinateAgent.class ) )
    {
      return new AssassinateAgentAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( AssassinateDiplomat.class ) )
    {
      return new AssassinateDiplomatAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( AssassinateWizard.class ) )
    {
      return new AssassinateWizardAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( SabotageEmbassy.class ) )
    {
      return new AgentSabotageEmbassyAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( ImproveEmbassy.class ) )
    {
      return new ImproveEmbassyAction( playerDetailsPane, (King) item );
    }
    else if ( orderType.equals( MapRegion.class ) )
    {
      return new MapRegionAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( TakeRegionCensus.class ) )
    {
      return new TakeRegionCensusAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( AbstractArmyAttack.class ) )
    {
      return new ArmyAttackAction( playerDetailsPane, (Army) item );
    }
    else if ( orderType.equals( Scry.class ) )
    {
      return new ScryAction( playerDetailsPane, (Wizard) item );
    }
    else if ( orderType.equals( TeleportSelf.class ) )
    {
      return new TeleportSelfAction( playerDetailsPane, (Wizard) item );
    }
    else
    {
      return null;
    }
  }

  abstract static class SingleTargetAction<S extends GameItem, T> extends OrderAction implements TargetAction
  {
    protected final S _subject;
    protected T _target;
    protected final PlayerDetailsPane _playerDetailsPane;
    protected TargetState _targetState = TargetState.INITIAL;
    protected SingleTargetChooser _targetChooser;

    SingleTargetAction( PlayerDetailsPane playerDetailsPane, S subject, String actionName )
    {
      super( actionName );
      _subject = subject;
      this._playerDetailsPane = playerDetailsPane;
    }

    public void actionPerformed( ActionEvent e )
    {
      switch ( _targetState )
      {
        case INITIAL:
          _playerDetailsPane.setActionInProgress( this );
          _targetChooser = new SingleTargetChooser( _playerDetailsPane._gameDisplay, this );
          switchToTargetCursor();
          _targetState = TargetState.SELECTING;
          break;
        case SELECTING: // Handles selection from a popup of choices.
          if ( e.getSource() instanceof SingleTargetChooser.SingleTargetMenuItem )
          {
            targetAcquired( (T) ((SingleTargetChooser.SingleTargetMenuItem) e.getSource()).getItem() );
          }
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

    public void targetAcquired( T target )
    {
      cancelTargetAcquisition();

      _target = target;
      Order order = createOrder();
      _subject.addNextTurnOrder( order );
      _playerDetailsPane.updateOrders( order );
      _playerDetailsPane.updateDetails();
      _playerDetailsPane._gameDisplay._mapPane.repaint();
    }

    public void cancelTargetAcquisition()
    {
      _playerDetailsPane.setActionInProgress( null );
      _targetState = TargetState.INITIAL;
      switchToNormalCursor();
    }

    public TargetListener getTargetChooser() { return _targetChooser; }

    public TargetState getTargetState() { return _targetState; }

    public boolean isQuickTarget() { return false; }

    protected abstract Order createOrder();

    /**
     * Returns a list of targets at the hex if a choice needs to be made. If the choice doesn't need to be made because the hex itself
     * is a valid target, then target selection occurs automatically.
     *
     * @return the targets at the hex if a choice needs to be made or empty list if no choice needs to be made.
     */
    public abstract List<T> checkForTargetsAtHex( Tuple hex );
  }

  static class AgentReconLocationAction extends SingleTargetAction<Agent, Tuple>
  {
    AgentReconLocationAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    {
      super( playerDetailsPane, agent, "Recon Location" );
    }

    protected Order createOrder()
    {
      return new AgentReconLocation( _subject, _target );
    }

    public List<Tuple> checkForTargetsAtHex( Tuple hex )
    {
      List<Tuple> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        targets.add( hex );
      }
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  abstract static class AbstractAgentRegionalScoutAction extends SingleTargetAction<Agent, PopCenter>
  {
    AbstractAgentRegionalScoutAction( PlayerDetailsPane playerDetailsPane, Agent agent, String description )
    {
      super( playerDetailsPane, agent, description );
    }

    public List<PopCenter> checkForTargetsAtHex( Tuple hex )
    {
      List<PopCenter> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( item instanceof PopCenter )
          {
            if ( ((PopCenter) item).getType() == PopCenter.PopType.City )
            {
              targets.add( (PopCenter) item );
              break;
            }
          }
        }
      }
      doTargetFoundCheck( _playerDetailsPane, targets, "Select the regional city to select a region." );
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  static class AgentScoutEmbassiesAction extends AbstractAgentRegionalScoutAction
  {
    AgentScoutEmbassiesAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Recon Embassies" );}

    protected Order createOrder() { return new AgentScoutEmbassies( _subject, _target ); }
  }

  static class AgentScoutControlLevelAction extends AbstractAgentRegionalScoutAction
  {
    AgentScoutControlLevelAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Recon Control Level" ); }

    protected Order createOrder() { return new AgentScoutControlLevel( _subject, _target ); }
  }

  static class AgentSabotagePopCenterAction extends SingleTargetAction<Agent, PopCenter>
  {
    AgentSabotagePopCenterAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    {
      super( playerDetailsPane, agent, "Sabotage Pop Center" );
    }

    protected Order createOrder()
    {
      return new SabotagePopCenter( _subject, _target );
    }

    public List<PopCenter> checkForTargetsAtHex( Tuple hex )
    {
      List<PopCenter> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( item instanceof PopCenter )
          {
            targets.add( (PopCenter) item );
            break;
          }
        }
        doTargetFoundCheck( _playerDetailsPane, targets, "There is no PopCenter to sabotage at " + hex );
      }
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  abstract static class MoveFigureAction<X extends Figure> extends SingleTargetAction<X, GameItem>
  {
    protected MoveFigureAction( PlayerDetailsPane playerDetailsPane, X figure )
    {
      super( playerDetailsPane, figure, "Move" );
    }

    public List<GameItem> checkForTargetsAtHex( Tuple hex )
    {
      List<GameItem> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( (item instanceof PopCenter) ||
               ((item instanceof Army) && item.getOwner().equals( _playerDetailsPane.getPlayer() )) )
          {
            targets.add( item );
          }
        }
        doTargetFoundCheck( _playerDetailsPane, targets, "There is no legal destination at " + hex );
      }
      return targets;
    }
  }

  static class MoveAgentAction extends MoveFigureAction<Agent>
  {
    MoveAgentAction( PlayerDetailsPane playerDetailsPane, Agent agent ) { super( playerDetailsPane, agent ); }

    protected Order createOrder() { return new MoveAgent( _subject, _target ); }
  }

  static class MoveDiplomatAction extends MoveFigureAction<Diplomat>
  {
    MoveDiplomatAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat ) { super( playerDetailsPane, diplomat ); }

    protected Order createOrder() { return new MoveDiplomat( _subject, _target ); }
  }

  static class MoveWizardAction extends MoveFigureAction<Wizard>
  {
    MoveWizardAction( PlayerDetailsPane playerDetailsPane, Wizard wizard ) { super( playerDetailsPane, wizard ); }

    protected Order createOrder() { return new MoveWizard( _subject, _target ); }
  }

  abstract static class AssassinateFigureAction<X extends Figure> extends SingleTargetAction<Agent, X>
  {
    private final Class _figureType;

    AssassinateFigureAction( PlayerDetailsPane playerDetailsPane, Agent agent, String description, Class figureType )
    {
      super( playerDetailsPane, agent, description );
      _figureType = figureType;
    }

    @SuppressWarnings({ "ChainOfInstanceofChecks" })
    protected Order createOrder()
    {
      if ( _target instanceof Agent )
      {
        return new AssassinateAgent( _subject, (Agent) _target, _target.getBase() );
      }
      else if ( _target instanceof Diplomat )
      {
        return new AssassinateDiplomat( _subject, (Diplomat) _target, _target.getBase() );
      }
      else if ( _target instanceof Wizard )
      {
        return new AssassinateWizard( _subject, (Wizard) _target, _target.getBase() );
      }
      else
      {
        throw new RuntimeException( "Invalid assassination target: " + _target.getLongStatusName() );
      }
    }

    public List<X> checkForTargetsAtHex( Tuple hex )
    {
      List<X> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( item.getOwner().equals( _playerDetailsPane.getPlayer() ) ) continue;
          if ( _figureType.isAssignableFrom( item.getClass() ) )
          {
            targets.add( (X) item );
          }
        }
        doTargetFoundCheck( _playerDetailsPane, targets, "There are no known targets at " + hex );
      }
      return targets;
    }
  }

  static class AssassinateAgentAction extends AssassinateFigureAction<Agent>
  {
    AssassinateAgentAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Assassinate Agent", Agent.class ); }
  }

  static class AssassinateDiplomatAction extends AssassinateFigureAction<Diplomat>
  {
    AssassinateDiplomatAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Assassinate Diplomat", Diplomat.class ); }
  }

  static class AssassinateWizardAction extends AssassinateFigureAction<Wizard>
  {
    AssassinateWizardAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Assassinate Wizard", Wizard.class ); }
  }

  static class AgentSabotageEmbassyAction extends SingleTargetAction<Agent, String>
  {
    PopCenter _regionalCity;

    AgentSabotageEmbassyAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    {
      super( playerDetailsPane, agent, "Sabotage Embassy" );
    }

    protected Order createOrder()
    {
      return new SabotageEmbassy( _subject, _regionalCity, _target );
    }

    public List<String> checkForTargetsAtHex( Tuple hex )
    {
      List<String> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( (item instanceof PopCenter) && ((PopCenter) item).getType() == PopCenter.PopType.City )
          {
            _regionalCity = (PopCenter) item;
            targets.addAll( _playerDetailsPane.getPlayer().getGameView().getPlayerNames() );
            targets.remove( _playerDetailsPane.getPlayer().getName() );
            break;
          }
        }
        doTargetFoundCheck( _playerDetailsPane, targets, "Select the regional city to pick a target player's embassy." );
      }
      return targets;
    }
  }

  static class ImproveEmbassyAction extends SingleTargetAction<King, String>
  {
    ImproveEmbassyAction( PlayerDetailsPane playerDetailsPane, King king )
    {
      super( playerDetailsPane, king, "Improve Embassy" );
    }

    protected Order createOrder()
    {
      return new ImproveEmbassy( _subject, _target );
    }

    public List<String> checkForTargetsAtHex( Tuple hex )
    {
      List<String> targets = new ArrayList<>();
      for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
      {
        if ( (item instanceof PopCenter) && (((PopCenter) item).getType() == PopCenter.PopType.City) )
        {
          targets.add( item.getLocation().getRegion().getName() );
          break;
        }
      }
      doTargetFoundCheck( _playerDetailsPane, targets, "Select the regional city to improve the embassy in that region." );
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  static class MapRegionAction extends SingleTargetAction<Diplomat, PopCenter>
  {
    MapRegionAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat )
    {
      super( playerDetailsPane, diplomat, "Map Region" );
    }

    protected Order createOrder()
    {
      return new MapRegion( _subject, _target );
    }

    public List<PopCenter> checkForTargetsAtHex( Tuple hex )
    {
      List<PopCenter> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( item instanceof PopCenter )
          {
            if ( ((PopCenter) item).getType() == PopCenter.PopType.City )
            {
              targets.add( (PopCenter) item );
              break;
            }
          }
        }
      }
      doTargetFoundCheck( _playerDetailsPane, targets, "Select the regional city to select a region to map." );
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  static class TakeRegionCensusAction extends SingleTargetAction<Diplomat, PopCenter.PopType>
  {
    PopCenter _regionalCity;

    TakeRegionCensusAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat )
    {
      super( playerDetailsPane, diplomat, "Take Region Census" );
    }

    protected Order createOrder()
    {
      return new TakeRegionCensus( _subject, _regionalCity, _target );
    }

    public List<PopCenter.PopType> checkForTargetsAtHex( Tuple hex )
    {
      List<PopCenter.PopType> targets = new ArrayList<>();
      if ( passesRangeCheck( _playerDetailsPane, _subject, hex ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          if ( item instanceof PopCenter )
          {
            if ( ((PopCenter) item).getType() == PopCenter.PopType.City )
            {
              _regionalCity = (PopCenter) item;
              targets = new ArrayList<>( Arrays.asList( PopCenter.PopType.values() ) );
              targets.remove( PopCenter.PopType.Unknown );
              break;
            }
          }
        }
      }
      doTargetFoundCheck( _playerDetailsPane, targets, "Select the regional city to select a region to take census." );
      return targets;
    }
  }

  static class ArmyAttackAction extends SingleTargetAction<Army, CombatCapableItem>
  {
    ArmyAttackAction( PlayerDetailsPane playerDetailsPane, Army army )
    {
      super( playerDetailsPane, army, "Attack" );
    }

    protected Order createOrder()
    {
      if ( _target instanceof Army )
      {
        return new ArmyAttackArmy( _subject, (Army) _target );
      }
      else
      {
        return new ArmyAttackPop( _subject, (PopCenter) _target );
      }
    }

    public List<CombatCapableItem> checkForTargetsAtHex( Tuple hex )
    {
      List<CombatCapableItem> targets = new ArrayList<>();
      if ( hex.equals( _subject.getLocation().getCoord() ) )
      {
        for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
        {
          boolean isCombatItem = item instanceof CombatCapableItem;
          boolean isNotMyItem = !item.getOwner().equals( _playerDetailsPane.getPlayer() );
          if ( isCombatItem && isNotMyItem && isNewTarget( item ) )
          {
            targets.add( (CombatCapableItem) item );
          }
        }
        doTargetFoundCheck( _playerDetailsPane, targets, "There is no valid target in our location " + hex );
      }
      else
      {
        JOptionPane.showMessageDialog( _playerDetailsPane._gameDisplay, "We can only attack targets in our hex." );
      }

      return targets;
    }

    private boolean isNewTarget( GameItem item )
    {
      for ( Order order : _subject.getNextTurnOrders() )
      {
        if ( (order instanceof AbstractArmyAttack) && (((AbstractArmyAttack) order).getTarget().equals( item )) )
        {
          return false;
        }
      }
      return true;
    }
  }

  static class ScryAction extends SingleTargetAction<Wizard, Tuple>
  {
    ScryAction( PlayerDetailsPane playerDetailsPane, Wizard wizard )
    {
      super( playerDetailsPane, wizard, "Scry Location" );
    }

    protected Order createOrder()
    {
      return new Scry( _subject, _target );
    }

    public List<Tuple> checkForTargetsAtHex( Tuple hex )
    {
      List<Tuple> targets = new ArrayList<>();
      targets.add( hex );
      return targets;
    }

    public boolean isQuickTarget() { return true; }
  }

  static class TeleportSelfAction extends SingleTargetAction<Wizard, GameItem>
  {
    protected TeleportSelfAction( PlayerDetailsPane playerDetailsPane, Wizard figure )
    {
      super( playerDetailsPane, figure, "Teleport Self" );
    }

    public List<GameItem> checkForTargetsAtHex( Tuple hex )
    {
      List<GameItem> targets = new ArrayList<>();
      for ( GameItem item : _playerDetailsPane._gameDisplay.getItemsAtHex( hex ) )
      {
        if ( (item instanceof PopCenter) ||
             ((item instanceof Army) && item.getOwner().equals( _playerDetailsPane.getPlayer() )) )
        {
          targets.add( item );
        }
      }
      doTargetFoundCheck( _playerDetailsPane, targets, "There is no legal destination at " + hex );
      return targets;
    }

    protected Order createOrder() { return new TeleportSelf( _subject, _target ); }
  }

  static boolean passesRangeCheck( PlayerDetailsPane detailsPane, Figure subject, Tuple targetHex )
  {
    int distance = HexCalculator.calculateDistance( targetHex, subject.getLocation().getCoord() );
    if ( distance > subject.getRange() )
    {
      JOptionPane.showMessageDialog( detailsPane._gameDisplay, "The destination " + targetHex + " is beyond my range." );
      return false;
    }
    return true;
  }

  private static boolean doTargetFoundCheck( PlayerDetailsPane detailsPane, List targets, String message )
  {
    if ( targets.isEmpty() )
    {
      JOptionPane.showMessageDialog( detailsPane._gameDisplay, message );
      return true;
    }
    return false;
  }
}
