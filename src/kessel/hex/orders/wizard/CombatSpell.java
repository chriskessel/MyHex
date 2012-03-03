package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.CombatCapableItem;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Common functionality for combat spells. Combat spells require a target that's in the same location. */
@SuppressWarnings({ "ClassReferencesSubclass" })
public abstract class CombatSpell extends AbstractSpell
{
  // Use for json persistence.
  public static final String TARGET_ID_JSON = "targetId";

  protected GameItem _target;
  protected transient Integer _jsonTargetId;
  protected transient boolean _targetIsMissing = false;

  protected CombatSpell() { super(); } // GSON only
  protected CombatSpell( Wizard wizard, GameItem target )
  {
    super( wizard );
    _target = target;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotInSameLocation( game ) ) return false;
    return true;
  }

  protected boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "The target does not exist." );
      return true;
    }
    return false;
  }

  protected boolean handleTargetNotInSameLocation( Game game )
  {
    if ( !_subject.getLocation().equals( _target.getLocation() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " did not cast a combat spell since " + _target.getName() +
                                      " was not in the same location." );
      return true;
    }
    return false;
  }

  public GameItem getTarget() { return _target; }

  /** @return the value of the spell, though it's meaning is context dependent on the type of spell. */
  protected abstract int getValue();


  /** @return any dispel orders relevant for this battle. */
  public static List<DispelCombatMagic> getDispelCombatMagicOrders( Game game, CombatCapableItem subject, CombatCapableItem target )
  {
    List<DispelCombatMagic> toReturn = new ArrayList<>();
    List<DispelCombatMagic> dispelMagicOrders = game.getCurrentTurn().getOrdersOfType( DispelCombatMagic.class );
    for ( DispelCombatMagic dispelMagicOrder : dispelMagicOrders )
    {
      if ( dispelMagicOrder.getSubject().getBase().equals( subject ) &&
           dispelMagicOrder.getTarget().equals( target ) )
      {
        toReturn.add( dispelMagicOrder );
      }
    }
    return toReturn;
  }

  /** A helper to calculate if any of a list of dispel orders is successful against another combat spell. */
  public static boolean dispelSucceeds(
    Game game, CombatCapableItem dispelCaster, CombatCapableItem dispelTarget, CombatSpell spellToDispel )
  {
    // Each dispel order by the attacker has a chance of negating the combat spell.
    for ( DispelCombatMagic dispelOrder : getDispelCombatMagicOrders( game, dispelCaster, dispelTarget ) )
    {
      double levelRatio = (double) dispelOrder.getSubject().getLevel() / (double) spellToDispel.getSubject().getLevel();
      int dispelChance = (int) (levelRatio * 50.0);
      if ( new Random().nextInt( 100 ) < dispelChance )
      {
        return true;
      }
    }
    return false;
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _target = game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }
}
