package kessel.hex.orders;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameEvent;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.orders.wizard.CharmFigure;
import kessel.hex.orders.wizard.ShieldFigure;
import kessel.hex.orders.wizard.ShieldRegion;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a specific order in the game. */
@SuppressWarnings({ "ClassReferencesSubclass" })
public abstract class Order<T extends GameItem>
{
  private static final Logger LOG = Logger.getLogger( Order.class );

  // Represents a blank order (i.e. no order given).
  @SuppressWarnings({ "unchecked" })
  public static final Order<GameItem> NEW_ORDER = new Order()
  {
    protected void processOrder( Game game ) {}
    public String getShortDescription() { return "Add order"; }
    public void fixDeserializationReferences( Game game ) {}
  };

  // The game item the order is for.
  protected T _subject;
  protected transient String _subjectName;
  protected transient String _ownerName;
  protected transient Integer _jsonSubjectId;

  protected transient boolean _wasExecuted = false;
  protected transient boolean _subjectMissing = false;

  // Various information that helps track/describe any player visible events related to the attempted order execution.
  protected Map<Player, GameEvent> _gameEvents = new HashMap<>();

  protected Order() {} // for GSON only

  protected Order( T subject )
  {
    _subject = subject;
  }

  /** Execute the order on the given game. Few, if any, subclasses would override this. */
  public void execute( Game game )
  {
    if ( canExecute( game ) )
    {
      _wasExecuted = true;
      processOrder( game );
      updateViews( game );
      handleOrderCost();
      trackOrderExecuted();
      game.getCurrentTurn().addOrderExecuted( this );
    }
    publishEvents();
  }

  /** Tracks who executed the order. Usually it's the subject, but there are exceptions to the rule. */
  protected void trackOrderExecuted()
  {
    _subject.addOrderExecuted( this );
  }

  /**
   * Subclasses should almost always call super.canExecute() first before adding in their own checks.
   *
   * @return true if the order can be executed.
   */
  protected boolean canExecute( Game game )
  {
    return !handleSubjectMissing( game );
  }

  /** Is the figure still around? Could have been killed/kidnapped/etc. */
  protected boolean handleSubjectMissing( Game game )
  {
    if ( _subjectMissing )
    {
      addPlayerEvent( game, game.getPlayer( _ownerName ),
        _subjectName + " was not available and couldn't execute the order.", Location.NOWHERE );
      return true;
    }
    return false;
  }

  /** @returns true if the subject is a figure as was charmed. */
  protected boolean handleIsCharmed( Game game )
  {
    // Any figure, other than wizards, can be charmed.
    if ( _subject instanceof Figure && !(_subject instanceof Wizard) )
    {
      if ( isCharmed( game, (Figure) _subject ) )
      {
        addPlayerEvent( game, _subject, _subject.getName() + " is charmed and unable to execute their action." );
        return true;
      }
    }
    return false;
  }

  /**
   * @param target the candidate to check.
   * @return true if the subject is the victim of a charm spell.
   */
  protected boolean isCharmed( Game game, Figure target )
  {
    List<CharmFigure> charmOrders = game.getCurrentTurn().getOrdersOfType( CharmFigure.class );
    for ( CharmFigure charmOrder : charmOrders )
    {
      if ( charmOrder.getTarget().equals( target ) )
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @param target the candidate to check.
   * @return true if the subject is warded by a protection spell.
   */
  protected boolean isFigureProtected( Game game, Figure target )
  {
    List<ShieldFigure> shieldOrders = game.getCurrentTurn().getOrdersOfType( ShieldFigure.class );
    for ( ShieldFigure shieldOrder : shieldOrders )
    {
      if ( shieldOrder.getTarget().equals( target ) )
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @param region the region to check.
   * @return true if the region is protected by a ShieldRegion spell.
   */
  protected boolean isRegionProtected( Game game, Region region )
  {
    List<ShieldRegion> shieldOrders = game.getCurrentTurn().getOrdersOfType( ShieldRegion.class );
    for ( ShieldRegion shieldOrder : shieldOrders )
    {
      if ( shieldOrder.getSubject().getLocation().getRegion().equals( region ) )
      {
        return true;
      }
    }
    return false;
  }

  protected boolean handleAlreadyActedThisTurn( Game game )
  {
    if ( !_subject.getOrdersExecuted().isEmpty() )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " has already executed an order this turn." );
      return true;
    }
    return false;
  }

  protected void addPlayerEvent( Game game, GameItem item, String message )
  {
    addPlayerEvent( game, item.getOwner(), message, item.getLocation() );
  }

  protected void addPlayerEvent( Game game, GameItem item, String message, Location location )
  {
    addPlayerEvent( game, item.getOwner(), message, location );
  }

  protected void addPlayerEvent( Game game, Player player, String message, Location location )
  {
    GameEvent event = new GameEvent( message, location, game.getTurn() );
    _gameEvents.put( player, event );
    LOG.debug( event.toString() );
  }

  /** Execute the order itself. */
  protected abstract void processOrder( Game game );

  /** Update any player views that would be updated as a result of the order execution. E.g. an agent's recon results. */
  protected void updateViews( Game game )
  {
    // Subclasses should override if they have view updates.
  }

  private void publishEvents()
  {
    for ( Map.Entry<Player, GameEvent> entry : _gameEvents.entrySet() )
    {
      entry.getKey().addGameEvent( entry.getValue() );
    }
  }

  /** @return How much the order costs to execute. */
  public int getOrderCost()
  { return 0; }

  /** Adjust for the cost of executing the order. */
  protected void handleOrderCost()
  {
    _subject.getOwner().adjustGold( getOrderCost() * -1 );
  }

  /** @return true if the order was executed. Note that this doesn't say if it was successful. */
  public boolean wasExecuted()
  { return _wasExecuted; }

  public T getSubject() { return _subject; }

  /**
   * @return the location the order is executed from. This is usually the subject of the location, but not always (e.g. ImprovePopCenter
   *         has theKing as the subject, but the town is the location).
   */
  public Location getOrderLocation()
  {
    return _subject.getLocation();
  }

  /** A short description of the order */
  public abstract String getShortDescription();

  public boolean equals( Object o )
  {
    return EqualsBuilder.reflectionEquals( this, o );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this );
  }

  /** @return a map of name/value pairs to serialize for persistence. */
  protected Map<String, Object> getSerializationItems()
  {
    Map<String, Object> map = new HashMap<>();
    map.put( MyJsonAdapter.ORDER_TYPE, getClass().getName() );
    map.put( MyJsonAdapter.SUBJECT_ID, _subject.getId() );
    map.put( MyJsonAdapter.SUBJECT_NAME, _subject.getName() );
    map.put( MyJsonAdapter.SUBJECT_OWNER, _subject.getOwner().getName() );
    return map;
  }

  public static class MyJsonAdapter<X extends Order> implements JsonSerializer<X>, JsonDeserializer<X>
  {
    // Used for json persistence.
    public static final String ORDER_TYPE = "type";
    public static final String SUBJECT_ID = "subject";
    public static final String SUBJECT_NAME = "subjectName";
    public static final String SUBJECT_OWNER = "ownerName";

    public JsonElement serialize( X order, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      Map<String, Object> fieldsToSerialize = order.getSerializationItems();
      for ( Map.Entry<String, Object> entry : fieldsToSerialize.entrySet() )
      {
        jsonResponse.add( entry.getKey(), context.serialize( entry.getValue() ) );
      }
      return jsonResponse;
    }

    public X deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      // Figure out the order type and defer to it's specific deserializer.
      String className = jsonOrder.getAsJsonObject().get( ORDER_TYPE ).getAsString();
      try
      {
        Class<?> orderClass = Class.forName( className );
        X order = (X) orderClass.newInstance();
        order.doDeserialize( jsonOrder, context );
        return order;

      }
      catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e )
      {
        throw new RuntimeException( "Failure loading order of type: " + className, e );
      }
    }
  }

  // Called by subclasses to deserialize basic Order items.
  public void doDeserialize( JsonElement jsonOrder, JsonDeserializationContext context )
  {
    _jsonSubjectId = context.deserialize( jsonOrder.getAsJsonObject().get( MyJsonAdapter.SUBJECT_ID ), Integer.class );
    _subjectName = context.deserialize( jsonOrder.getAsJsonObject().get( MyJsonAdapter.SUBJECT_NAME ), String.class );
    _ownerName = context.deserialize( jsonOrder.getAsJsonObject().get( MyJsonAdapter.SUBJECT_OWNER ), String.class );
  }

  public void fixDeserializationReferences( Game game )
  {
    _subject = (T) game.getItem( _jsonSubjectId );
    if ( _subject == null ) { _subjectMissing = true; }
  }
}
