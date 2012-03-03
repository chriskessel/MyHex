package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Agent;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.agent.AbstractAssassinateFigure;
import kessel.hex.orders.agent.AssassinateAgent;
import kessel.hex.orders.agent.AssassinateDiplomat;
import kessel.hex.orders.agent.AssassinateWizard;

import java.util.Map;

/** Summons a level 15 assassin for a single assassinate attempt. */
public class ShadowAssassin extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";
  public static final String TARGET_BASE_ID_JSON = "targetBaseId";

  protected Figure _target;
  protected transient Integer _jsonTargetId;
  protected GameItem _targetBase;
  protected transient Integer _jsonTargetBaseId;

  protected transient boolean _targetIsMissing = false;
  protected transient boolean _targetBaseIsMissing = false;
  protected transient boolean _wasSuccessful = false;

  public ShadowAssassin() { super(); } // GSON only
  public ShadowAssassin( Wizard wizard, Figure target, GameItem targetBase )
  {
    super( wizard );
    _target = target;
    _targetBase = targetBase;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetBaseMissing( game ) ) return false;
    if ( handleTargetNotAtBase( game ) ) return false;
    if ( handleTargetProtected( game ) ) return false;
    return true;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, _subject.getName() + "'s shadow assassin was unable to locate the target." );
      return true;
    }
    return false;
  }

  private boolean handleTargetBaseMissing( Game game )
  {
    if ( _targetBaseIsMissing )
    {
      addPlayerEvent( game, _subject, _subject.getName() + "'s shadow assassin was unable to locate the target's base." );
      return true;
    }
    return false;
  }

  private boolean handleTargetNotAtBase( Game game )
  {
    if ( !_targetBase.equals( _target.getBase() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + "'s shadow assassin did not find " +
                                      _target.getName() + " in " + _targetBase.getName() + "." );
      return true;
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isFigureProtected( game, _target ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + "'s shadow assassin could not affect the protected target " +
                                      _target.getName() + "(" + _target.getOwnerName() + ")" );
      return true;
    }
    return false;
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  protected void processOrder( Game game )
  {
    Agent shadowAssassin = new Agent( GameItem.VALUE_UNKNOWN, "Shadow", game.getTurn(), _subject.getBase(), _subject.getOwner() );
    shadowAssassin.setLevel( 12 );
    AbstractAssassinateFigure assassinOrder;
    if ( _target instanceof Agent )
    {
      assassinOrder = new AssassinateAgent( shadowAssassin, (Agent) _target, _targetBase );
    }
    else if ( _target instanceof Diplomat )
    {
      assassinOrder = new AssassinateDiplomat( shadowAssassin, (Diplomat) _target, _targetBase );
    }
    else if ( _target instanceof Wizard )
    {
      assassinOrder = new AssassinateWizard( shadowAssassin, (Wizard) _target, _targetBase );
    }
    else
    {
      throw new RuntimeException( "Invalid target type: " + _target.getClass() );
    }
    _wasSuccessful = assassinOrder.makeAttempt( game );
    publishCasterEvent( game );
    if ( _wasSuccessful )
    {
      _target.getOwner().remove( _target );
      publishVictimEvent( game );
    }
  }

  private void publishCasterEvent( Game game )
  {
    String message = _subject.getName() + "'s shadow assassin " + (_wasSuccessful ? "killed " : "failed to kill ") +
                     _target.getName() + "(" + _target.getOwnerName() + ").";
    addPlayerEvent( game, _subject, message );
  }

  private void publishVictimEvent( Game game )
  {
    String message = _target.getName() + " was killed by a magical shadow assassin!";
    addPlayerEvent( game, _subject, message, _target.getLocation() );
  }

  protected void updateViews( Game game )
  {
    // If the mission succeeded, the wizard's owner knows the target is gone from the game.
    if ( _wasSuccessful )
    {
      _subject.getOwner().removeKnownItem( _target );
    }
  }

  public boolean wasSuccessful() { return _wasSuccessful; }

  public String getShortDescription()
  {
    return _subject.getName() + " send shadow assassin after " + _target.getShortStatusName() +
           " at " + _target.getLocation().getCoord();
  }

  public Figure getTarget() { return _target; }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    map.put( TARGET_BASE_ID_JSON, _targetBase.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
    _jsonTargetBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_BASE_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = (Figure) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
    _targetBase = game.getItem( _jsonTargetBaseId );
    if ( _targetBase == null ) { _targetBaseIsMissing = true; }
  }
}
