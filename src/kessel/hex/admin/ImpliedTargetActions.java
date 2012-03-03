package kessel.hex.admin;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.King;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.Order;
import kessel.hex.orders.agent.CounterEspionage;
import kessel.hex.orders.agent.HireAgent;
import kessel.hex.orders.agent.TrainAgent;
import kessel.hex.orders.army.ArmyRecruit;
import kessel.hex.orders.army.CreateArmy;
import kessel.hex.orders.diplomat.DiplomatInciteRebellion;
import kessel.hex.orders.diplomat.DiplomatInspireLoyalty;
import kessel.hex.orders.diplomat.DiplomatNegotiateFealty;
import kessel.hex.orders.diplomat.HireDiplomat;
import kessel.hex.orders.diplomat.TrainDiplomat;
import kessel.hex.orders.king.ImprovePopCenter;
import kessel.hex.orders.king.ImprovePower;
import kessel.hex.orders.wizard.Alchemy;
import kessel.hex.orders.wizard.HireWizard;
import kessel.hex.orders.wizard.ImbuePopCenter;
import kessel.hex.orders.wizard.TrainWizard;

import javax.swing.*;
import java.awt.event.ActionEvent;

/** A static holder for all the orders that don't need any additional information. */
public class ImpliedTargetActions
{
  /** @return the implied order or null if the order type isn't right for an implied order. */
  public static Action createAction( PlayerDetailsPane playerDetailsPane, Class orderType, GameItem item )
  {
    if ( orderType.equals( HireAgent.class ) )
    {
      return new ImpliedTargetActions.HireAgentAction( playerDetailsPane, (PopCenter) item );
    }
    else if ( orderType.equals( HireDiplomat.class ) )
    {
      return new ImpliedTargetActions.HireDiplomatAction( playerDetailsPane, (PopCenter) item );
    }
    else if ( orderType.equals( HireWizard.class ) )
    {
      return new ImpliedTargetActions.HireWizardAction( playerDetailsPane, (PopCenter) item );
    }
    else if ( orderType.equals( TrainAgent.class ) )
    {
      return new ImpliedTargetActions.TrainAgentAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( TrainDiplomat.class ) )
    {
      return new ImpliedTargetActions.TrainDiplomatAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( TrainWizard.class ) )
    {
      return new ImpliedTargetActions.TrainWizardAction( playerDetailsPane, (Wizard) item );
    }
    else if ( orderType.equals( DiplomatInciteRebellion.class ) )
    {
      return new ImpliedTargetActions.DiplomatInciteRebellionAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( DiplomatInspireLoyalty.class ) )
    {
      return new ImpliedTargetActions.DiplomatInspireLoyaltyAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( DiplomatNegotiateFealty.class ) )
    {
      return new ImpliedTargetActions.DiplomatNegotiateFealtyAction( playerDetailsPane, (Diplomat) item );
    }
    else if ( orderType.equals( ImprovePopCenter.class ) )
    {
      return new ImpliedTargetActions.ImprovePopCenterAction( playerDetailsPane, (PopCenter) item );
    }
    else if ( orderType.equals( ImprovePower.class ) )
    {
      return new ImpliedTargetActions.ImprovePowerAction( playerDetailsPane, (King) item );
    }
    else if ( orderType.equals( CreateArmy.class ) )
    {
      return new ImpliedTargetActions.CreateArmyAction( playerDetailsPane, (King) item );
    }
    else if ( orderType.equals( ArmyRecruit.class ) )
    {
      return new ImpliedTargetActions.ArmyRecruitAction( playerDetailsPane, (Army) item );
    }
    else if ( orderType.equals( CounterEspionage.class ) )
    {
      return new ImpliedTargetActions.CounterEspionageAction( playerDetailsPane, (Agent) item );
    }
    else if ( orderType.equals( Alchemy.class ) )
    {
      return new ImpliedTargetActions.AlchemyAction( playerDetailsPane, (Wizard) item );
    }
    else if ( orderType.equals( ImbuePopCenter.class ) )
    {
      return new ImpliedTargetActions.ImbuePopCenterAction( playerDetailsPane, (Wizard) item );
    }
    else
    {
      return null;
    }
  }

  abstract static class ImpliedTargetAction extends OrderAction
  {
    protected final GameItem _subject;
    protected final PlayerDetailsPane _playerDetailsPane;

    ImpliedTargetAction( PlayerDetailsPane playerDetailsPane, GameItem subject, String actionName )
    {
      super( actionName );
      _subject = subject;
      this._playerDetailsPane = playerDetailsPane;
    }

    public void actionPerformed( ActionEvent e )
    {
      Order order = createOrder();
      _subject.addNextTurnOrder( order );
      _playerDetailsPane.updateOrders( order );
      _playerDetailsPane.updateDetails();
    }

    protected abstract Order createOrder();
  }

  static class HireAgentAction extends ImpliedTargetAction
  {
    HireAgentAction( PlayerDetailsPane playerDetailsPane, PopCenter base ) { super( playerDetailsPane, base, "Hire Agent" ); }

    protected Order createOrder() { return new HireAgent( _playerDetailsPane.getPlayer().getKing(), (PopCenter) _subject ); }
  }

  static class HireDiplomatAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    HireDiplomatAction( PlayerDetailsPane playerDetailsPane, PopCenter base ) { super( playerDetailsPane, base, "Hire Diplomat" ); }

    protected Order createOrder() { return new HireDiplomat( _playerDetailsPane.getPlayer().getKing(), (PopCenter) _subject ); }
  }

  static class HireWizardAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    HireWizardAction( PlayerDetailsPane playerDetailsPane, PopCenter base ) { super( playerDetailsPane, base, "Hire Wizard" ); }

    protected Order createOrder() { return new HireWizard( _playerDetailsPane.getPlayer().getKing(), (PopCenter) _subject ); }
  }

  static class ImprovePopCenterAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    ImprovePopCenterAction( PlayerDetailsPane playerDetailsPane, PopCenter pop ) { super( playerDetailsPane, pop, "Improve Level" ); }

    protected Order createOrder() { return new ImprovePopCenter( _playerDetailsPane.getPlayer().getKing(), (PopCenter) _subject ); }
  }

  static class ImprovePowerAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    ImprovePowerAction( PlayerDetailsPane playerDetailsPane, King king ) { super( playerDetailsPane, king, "Improve Power" ); }

    protected Order createOrder() { return new ImprovePower( (King) _subject ); }
  }

  static class DiplomatInciteRebellionAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    DiplomatInciteRebellionAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat )
    { super( playerDetailsPane, diplomat, "Incite Rebellion" ); }

    protected Order createOrder() { return new DiplomatInciteRebellion( (Diplomat) _subject ); }
  }

  static class DiplomatInspireLoyaltyAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    DiplomatInspireLoyaltyAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat )
    { super( playerDetailsPane, diplomat, "Inspire Loyalty" ); }

    protected Order createOrder() { return new DiplomatInspireLoyalty( (Diplomat) _subject ); }
  }

  static class DiplomatNegotiateFealtyAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    DiplomatNegotiateFealtyAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat )
    { super( playerDetailsPane, diplomat, "Negotiate Fealty" ); }

    protected Order createOrder() { return new DiplomatNegotiateFealty( (Diplomat) _subject ); }
  }

  static class TrainAgentAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    TrainAgentAction( PlayerDetailsPane playerDetailsPane, Agent agent ) { super( playerDetailsPane, agent, "Train" ); }

    protected Order createOrder() { return new TrainAgent( (Agent) _subject ); }
  }

  static class TrainDiplomatAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    TrainDiplomatAction( PlayerDetailsPane playerDetailsPane, Diplomat diplomat ) { super( playerDetailsPane, diplomat, "Train" ); }

    protected Order createOrder() { return new TrainDiplomat( (Diplomat) _subject ); }
  }

  static class TrainWizardAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    TrainWizardAction( PlayerDetailsPane playerDetailsPane, Wizard wizard ) { super( playerDetailsPane, wizard, "Train" ); }

    protected Order createOrder() { return new TrainWizard( (Wizard) _subject ); }
  }

  static class CreateArmyAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    CreateArmyAction( PlayerDetailsPane playerDetailsPane, King king ) { super( playerDetailsPane, king, "Create Army" ); }

    protected Order createOrder() { return new CreateArmy( (King) _subject ); }
  }

  static class ArmyRecruitAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    ArmyRecruitAction( PlayerDetailsPane playerDetailsPane, Army army ) { super( playerDetailsPane, army, "Recruit" ); }

    protected Order createOrder() { return new ArmyRecruit( (Army) _subject ); }
  }

  static class CounterEspionageAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    CounterEspionageAction( PlayerDetailsPane playerDetailsPane, Agent agent )
    { super( playerDetailsPane, agent, "Counter Espionage" ); }

    protected Order createOrder() { return new CounterEspionage( (Agent) _subject ); }
  }

  static class AlchemyAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    AlchemyAction( PlayerDetailsPane playerDetailsPane, Wizard wizard ) { super( playerDetailsPane, wizard, "Alchemy" ); }

    protected Order createOrder() { return new Alchemy( (Wizard) _subject ); }
  }

  static class ImbuePopCenterAction extends ImpliedTargetActions.ImpliedTargetAction
  {
    ImbuePopCenterAction( PlayerDetailsPane playerDetailsPane, Wizard wizard )
    { super( playerDetailsPane, wizard, "Imbue Pop Center" ); }

    protected Order createOrder() { return new Alchemy( (Wizard) _subject ); }
  }

  static class CancelAction extends AbstractAction
  {
    protected final GameItem _target;
    protected final Order<GameItem> _order;
    protected final PlayerDetailsPane _playerDetailsPane;

    CancelAction( PlayerDetailsPane playerDetailsPane, Order<GameItem> order, GameItem target )
    {
      super( "Cancel" );
      _target = target;
      _order = order;
      _playerDetailsPane = playerDetailsPane;
    }

    public void actionPerformed( ActionEvent e )
    {
      _target.removeNextTurnOrder( _order );
      _playerDetailsPane.updateDetails();
      _playerDetailsPane.updateOrders();
      _playerDetailsPane._gameDisplay.repaint();
    }
  }
}
