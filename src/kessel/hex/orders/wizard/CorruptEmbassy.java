package kessel.hex.orders.wizard;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import kessel.hex.domain.Game;
import kessel.hex.domain.Wizard;

import java.util.Map;

/** Damage a player's embassy in the wizard's region. */
public class CorruptEmbassy extends AbstractSpell
{
  public static final String PLAYER_ID_JSON = "playerName";

  protected String _playerName;

  public CorruptEmbassy() { super(); } // GSON only
  public CorruptEmbassy( Wizard wizard, String playerName )
  {
    super( wizard );
    _playerName = playerName;
  }

  @SuppressWarnings({ "IfStatementWithIdenticalBranches" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    else if ( handleBadPlayerName( game ) ) return false;
    else if ( handleAlreadyCorrupted( game ) ) return false;
    else if ( handleTargetProtected( game ) ) return false;
    return true;
  }

  private boolean handleBadPlayerName( Game game )
  {
    if ( game.getPlayer( _playerName ) == null )
    {
      addPlayerEvent( game, _subject, "The target player " + _playerName + " does not exist." );
      return true;
    }
    return false;
  }

  private boolean handleAlreadyCorrupted( Game game )
  {
    for ( CorruptEmbassy corruptEmbassy : game.getCurrentTurn().getOrdersOfType( CorruptEmbassy.class ) )
    {
      if ( corruptEmbassy._wasExecuted && corruptEmbassy._playerName.equals( _playerName ) &&
           corruptEmbassy._subject.getLocation().getRegion().equals( _subject.getLocation().getRegion() ) )
      {
        addPlayerEvent( game, _subject, _subject.getName() + " found the embassy already damaged this turn for " + _playerName +
                                        " in " + _subject.getLocation().getRegion().getName() );
        return true;
      }
    }
    return false;
  }

  private boolean handleTargetProtected( Game game )
  {
    if ( isRegionProtected( game, _subject.getLocation().getRegion() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " could not damage the embassy of " + _playerName +
                                      " because the region " + _subject.getLocation().getRegion().getName() +
                                      " was magically shielded." );
      return true;
    }
    return false;
  }

  protected void processOrder( Game game )
  {
    game.getPlayer( _playerName ).degradeEmbassy( _subject.getLocation().getRegion() );
  }

  public String getShortDescription()
  {
    return _subject.getName() + " magically damage the embassy of " + _playerName +
           " in " + _subject.getLocation().getRegion().getName();
  }

  protected void updateViews( Game game )
  {
    publishCasterEvent( game );
    publishVictimEvent( game );
  }

  private void publishCasterEvent( Game game )
  {
    addPlayerEvent( game, _subject, _subject.getName() + " magically damaged the embassy of " + _playerName +
                                    " in " + _subject.getLocation().getRegion().getName() );
  }

  private void publishVictimEvent( Game game )
  {
    String message = "Our embassy in " + _subject.getLocation().getRegion().getName() + " was magically damaged!";
    addPlayerEvent( game, game.getPlayer( _playerName ), message, _subject.getLocation() );
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( PLAYER_ID_JSON, _playerName );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _playerName = context.deserialize( jsonOrder.getAsJsonObject().get( PLAYER_ID_JSON ), String.class );
  }
}
