package kessel.hex.orders.army;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transfer stuff from one army to another. If the target is inactive army, it'll activate at the subject's location. If an army is
 * completely combined ny figures in the army have their base reset to the target.
 */
public class ArmyTransfer extends AbstractArmyOrder
{
  // Used for json persistence.
  public static final String TARGET_ID_JSON = "targetId";
  public static final String ITEM_IDS_JSON = "itemIds";

  protected Army _target;
  protected transient Integer _jsonTargetId;
  protected List<? extends GameItem> _transfers;
  protected transient List<Integer> _jsonTransferIds;

  protected transient boolean _targetIsMissing = false;
  protected transient boolean _invalidTransfers = false;

  public ArmyTransfer() { super(); } // GSON only
  public ArmyTransfer( Army army, Army target, List<? extends GameItem> transfers )
  {
    super( army );
    _target = target;
    _transfers = transfers;
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleInvalidTransfers( game ) ) return false;
    if ( handleTargetMissing( game ) ) return false;
    if ( handleTargetNotInSameLocation( game ) ) return false;
    return true;
  }

  private boolean handleInvalidTransfers( Game game )
  {
    if ( _invalidTransfers )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " tried to transfer ineligible (non-military) or non-existent items." );
      return true;
    }
    return false;
  }

  private boolean handleTargetMissing( Game game )
  {
    if ( _targetIsMissing )
    {
      addPlayerEvent( game, _subject, "Target was not available to accept the transfer." );
      return true;
    }
    return false;
  }

  private boolean handleTargetNotInSameLocation( Game game )
  {
    if ( _target.isActive() && !_target.getLocation().equals( _subject.getLocation() ) )
    {
      addPlayerEvent( game, _subject, _target.getName() + " is not at the same location to accept the transfer." );
      return true;
    }
    return false;
  }

  @SuppressWarnings({ "SuspiciousMethodCalls" })
  public void processOrder( Game game )
  {
    // If the target was inactive, it'll now become active at the location of the transfer.
    if ( !_target.isActive() )
    {
      _target.setLocation( _subject.getLocation() );
    }

    // Take each unit from the source and give it to the target.
    List<GameItem> tmpList = new ArrayList<>( _transfers );
    for ( GameItem transfer : tmpList )
    {
      if ( transfer instanceof ArmyUnit )
      {
        _subject.removeUnit( (ArmyUnit) transfer );
        _target.addUnit( (ArmyUnit) transfer );
      }
      else
      {
        // TODO - leaders, artifacts?
      }
    }

    // If everything was transferred the source goes inactive and back to the capitol.
    // Any figures in the now inactive subject switch the target as their base.
    if ( !_subject.isActive() )
    {
      _subject.setLocation( _subject.getOwner().getCapitol().getLocation() );
      for ( Figure figure : _subject.getOwner().getFigures() )
      {
        if ( figure.getBase().equals( _subject ) )
        {
          figure.setBase( _target );
        }
      }
    }
  }

  public String getShortDescription()
  {
    return _subject.getName() + " transfer " + _transfers.size() + " units to " + _target.getName();
  }

  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = super.getSerializationItems();
    map.put( TARGET_ID_JSON, _target.getId() );
    List<Integer> itemIds = new ArrayList<>();
    if ( _transfers != null )
    {
      for ( GameItem transfer : _transfers )
      {
        itemIds.add( transfer.getId() );
      }
    }
    map.put( ITEM_IDS_JSON, itemIds );
    return map;
  }

  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    super.doDeserialize( jsonOrder, context );
    _jsonTargetId = context.deserialize( jsonOrder.getAsJsonObject().get( TARGET_ID_JSON ), Integer.class );
    _jsonTransferIds = context.deserialize( jsonOrder.getAsJsonObject().get( ITEM_IDS_JSON ),  new TypeToken<List<Integer>>(){}.getType() );
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    fixTargetDeserializationReferences( game );
    fixTransferItemsDeserializationReferences();
  }

  private void fixTargetDeserializationReferences( Game game )
  {
    _target = (Army) game.getItem( _jsonTargetId );
    if ( _target == null ) { _targetIsMissing = true; }
  }

  private void fixTransferItemsDeserializationReferences()
  {
    List<GameItem> transfers = new ArrayList<>();
    if ( CollectionUtils.isEmpty( _jsonTransferIds ) )
    {
      _invalidTransfers = true;
    }
    else
    {
      for ( Integer transferId : _jsonTransferIds )
      {
        ArmyUnit unit = _subject.getArmyUnit( transferId );
        if ( unit != null )
        {
          transfers.add( unit );
        }
        // TODO - leaders? Artifacts?
      }
      if ( transfers.isEmpty() ) { _invalidTransfers = true; }
      _transfers = transfers;
    }
  }
}
