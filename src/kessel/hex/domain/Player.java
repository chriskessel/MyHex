package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.GameMap;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.orders.Order;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static kessel.hex.domain.GameItem.getGameItem;

/** A player in the game. */
@SuppressWarnings({ "RawUseOfParameterizedType", "PackageVisibleField" })
public class Player
{
  private static final Logger LOG = Logger.getLogger( Player.class );

  // Null Object pattern fake players when something needs to be flagged as not having a player or not knowing the player.
  public static final Player UNOWNED = createUnowned();
  public static final Player UNKNOWN = createUnknown();

  // Suffix for player persistence file.
  public static final String STATE_FILE = ".json";

  private static final int MIN_POWER = 8;

  /** The name of the player, which must be unique within a Game. */
  String _name;

  /** The kingdom the player rules. This never changes during a Game. */
  Kingdom _kingdom;

  /** The player's various GameItems. */
  PopCenter _capitol;
  Integer _jsonCapitolId;
  King _king;
  List<Wizard> _wizards = new ArrayList<>();
  List<Diplomat> _diplomats = new ArrayList<>();
  List<Agent> _agents = new ArrayList<>();
  List<Army> _armies = new ArrayList<>();
  List<PopCenter> _popCenters = new ArrayList<>();
  List<Integer> _jsonPopIds;

  int _gold;
  int _power;

  /** The number of kingdom units available for recruiting. */
  int _kingdomTroopsAvailable = 0;

  /** An index for which random name to assign to the next new Figure. */
  int _nextFigureName = 0;

  /** The player's embassy presence in each region, keyed by the region's name. */
  Map<String, Integer> _embassyLevels = new HashMap<>();

  /** The player's orders for the next turn. */
  List<Order> _turnOrders = new ArrayList<>();

  /** The history of game events known to the player. */
  List<GameEvent> _events = new ArrayList<>();

  /** The player's intelligence about regional control levels: Player->(Region,Level). */
  Map<String, Map<String, ControlLevel>> _controlLevelIntel = new HashMap<>();

  /** The player's view of the game. Each player has only limited knowledge of the full game information. */
  Game _gameView;

  public Player() {} // only to support GSON.
  public Player( String name )
  {
    this( name, null );
  }

  public Player( String name, Kingdom kingdom )
  {
    _name = name;
    _kingdom = kingdom;

    // Pick a random starting spot for names so we don't just use the same first few every game.
    _nextFigureName = new Random().nextInt( 1000 );
  }
  public static Player createUnowned() { return new Player( "Unowned"); }
  public static Player createUnknown() { return new Player( "Unknown"); }

  public String getName() { return _name; }

  public PopCenter getCapitol() { return _capitol; }

  public void setCapitol( PopCenter capitol ) { _capitol = capitol; }

  public Kingdom getKingdom() { return _kingdom; }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public void add( Figure figure )
  {
    if ( figure instanceof Agent )
    {
      _agents.add( (Agent) figure );
    }
    else if ( figure instanceof Diplomat )
    {
      _diplomats.add( (Diplomat) figure );
    }
    else if ( figure instanceof Wizard )
    {
      _wizards.add( (Wizard) figure );
    }
    else
    {
      throw new RuntimeException( "Invalid figure type to add: " + figure.getClass() );
    }
    figure.setOwner( this );
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public void remove( Figure figure )
  {
    if ( figure instanceof Agent )
    {
      _agents.remove( figure );
    }
    else if ( figure instanceof Diplomat )
    {
      _diplomats.remove( figure );
    }
    else if ( figure instanceof Wizard )
    {
      _wizards.remove( figure );
    }
    else
    {
      throw new RuntimeException( "Invalid figure type to remove: " + figure.getClass() );
    }
  }

  public List<Wizard> getWizards() { return _wizards; }

  public Wizard getWizard( int wizardId ) { return getGameItem( _wizards, wizardId ); }

  public List<Diplomat> getDiplomats() { return _diplomats; }

  public Diplomat getDiplomat( int diplomatId ) { return getGameItem( _diplomats, diplomatId ); }

  public List<Agent> getAgents() { return _agents; }

  public Agent getAgent( int agentId ) { return getGameItem( _agents, agentId ); }

  public List<Figure> getFigures()
  {
    List<Figure> figures = new ArrayList<>();
    figures.addAll( getWizards() );
    figures.addAll( getAgents() );
    figures.addAll( getDiplomats() );
    if ( getKing() != null ) { figures.add( getKing() ); }
    return figures;
  }

  private Figure getFigure( int id )
  {
    for ( Figure figure : getFigures() )
    {
      if ( figure.getId() == id )
      {
        return figure;
      }
    }
    return null;
  }

  public List<GameItem> getAllItems()
  {
    List<GameItem> items = new ArrayList<>();
    items.addAll( getPopCenters() );
    items.addAll( getFigures() );
    items.addAll( getArmies() );
    // TODO magic items
    return items;
  }

  @SuppressWarnings("UnusedDeclaration")
  public GameItem getKnownItem( int id ) { return _gameView.getItem( id ); }

  public List<GameItem> getKnownItems( Tuple hex ) { return _gameView.getAllGameItems( hex ); }

  public List<GameItem> getKnownItems() { return _gameView.getAllGameItems(); }

  @SuppressWarnings("UnusedDeclaration")
  public PopCenter getKnownPopCenter( int popId ) { return _gameView.getPopCenter( popId ); }

  @SuppressWarnings("UnusedDeclaration")
  public Army getKnownArmy( int armyId ) { return getArmy( armyId ); }

  public Map<Tuple, Location> getKnownLocations() { return _gameView.getMap().getLocationsByHex(); }

  public List<Region> getKnownRegions() { return _gameView.getMap().getRegions(); }

  public int getGold() { return _gold; }

  public void setGold( int gold ) { _gold = gold; }

  public void adjustGold( int adjustment ) { _gold += adjustment; }

  public int getPower() { return _power; }

  public void setPower( int power ) { _power = power; }

  public void adjustPower( int adjustment )
  {
    _power = Math.max( MIN_POWER, _power + adjustment );
  }

  public List<Army> getActiveArmies()
  {
    List<Army> list = new ArrayList<>();
    for ( Army army : _armies )
    {
      if ( army.isActive() )
      {
        list.add( army );
      }
    }
    return list;
  }

  public List<Army> getArmies() { return _armies; }

  public Army getArmy( int armyId )
  {
    for ( Army army : _armies )
    {
      if ( army.getId() == armyId )
      {
        return army;
      }
    }
    return null;
  }

  public void remove( Army army ) { _armies.remove( army ); }
  public void add( Army army )
  {
    _armies.add( army );
    army.setOwner( this );
  }

  public List<PopCenter> getPopCenters() { return _popCenters; }

  public void remove( PopCenter pop ) { _popCenters.remove( pop ); }
  public void add( PopCenter pop )
  {
    _popCenters.add( pop );
    pop.setOwner( this );
  }

  /** @return the player's item with the given id. */
  public GameItem getItem( int itemId )
  {
    List<GameItem> items = getAllItems();
    for ( GameItem item : items )
    {
      if ( item.getId() == itemId )
      {
        return item;
      }
    }
    return null;
  }

  public King getKing() { return _king; }

  public void setKing( King king ) { _king = king; }

  public Game getGameView() { return _gameView; }

  /** @return the orders next turn for the specific game item, an empty list if there are none. */
  public List<Order<GameItem>> getNextTurnOrders( GameItem item )
  {
    List<Order<GameItem>> orders = new ArrayList<>();
    for ( Order turnOrder : _turnOrders )
    {
      if ( turnOrder.getSubject().equals( item ) )
      {
        orders.add( turnOrder );
      }
    }
    return orders;
  }
  public List<Order> getNextTurnOrders() { return _turnOrders; }

  public void addOrder( Order order ) { _turnOrders.add( order ); }

  public void removeOrder( Order order ) { _turnOrders.remove( order ); }

  public void addGameEvent( GameEvent gameEvent ) { _events.add( gameEvent ); }

  public List<GameEvent> getGameEvents() { return _events; }

  public ControlLevel getControlLevel( Region region ) { return region.getControlLevel( this ); }

  /**
   * Relocate the player's capitol to a new population center. All figures that haven't already acted this turn move with it.
   *
   * @param target the new location
   */
  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public boolean relocateCapitol( PopCenter target )
  {
    if ( !target.getOwner().equals( this ) ) return false;

    PopCenter oldCapitol = _capitol;
    _capitol = target;

    // Any inactive armies are relocated to the new capitol.
    for ( Army army : _armies )
    {
      if ( !army.isActive() )
      {
        // NOTE: this does not count as a movement order for the army, unlike figures which do count this as a move.
        army.setLocation( _capitol.getLocation() );
      }
    }

    // Any of the player's other figures at the capitol, that haven't already done something, are moved as well and
    // it counts as the figure's order for the turn.
    for ( Figure figure : getFigures() )
    {
      if ( figure.getBase().equals( oldCapitol ) && figure.getOrdersExecuted().isEmpty() )
      {
        figure.forceMove( target );
      }
    }
    return true;
  }

  /**
   * The player establishes a new capitol under duress, randomly picking amongst the strongest population centers. If the player has no
   * population centers, he's "in the wild". Losing ones capitol always causes a power loss.
   */
  public void forcedCapitolRelocation()
  {
    remove( _capitol );
    if ( _popCenters.isEmpty() )
    {
      _capitol = PopCenter.THE_WILDS;
      _king.setBase( _capitol );

      // Any inactive armies are destroyed without a physical on-map capitol.
      for ( Iterator<Army> iterator = _armies.iterator(); iterator.hasNext(); )
      {
        Army next = iterator.next();
        if ( !next.isActive() )
        {
          iterator.remove();
        }
      }

      // Note: figures stay in the pop since the new capitol isn't a real location.
    }
    else
    {
      // Relocate to the strongest remaining pop center.
      List<PopCenter> strongestPops = new ArrayList<>();
      int strongestLevel = 0;
      for ( PopCenter popCenter : _popCenters )
      {
        if ( popCenter.getLevel() > strongestLevel )
        {
          strongestLevel = popCenter.getLevel();
          strongestPops.clear();
          strongestPops.add( popCenter );
        }
        else if ( popCenter.getLevel() == strongestLevel )
        {
          strongestPops.add( popCenter );
        }
      }
      Collections.shuffle( strongestPops );
      relocateCapitol( strongestPops.get( 0 ) );
    }
    adjustPower( -1 );
  }

  /** @return the amount of military the player can support. */
  public int getSupportCapacity()
  {
    double capacity = 0;
    for ( PopCenter popCenter : _popCenters )
    {
      switch ( popCenter.getType() )
      {

        case Hamlet:
          capacity += 3.75;
          break;
        case Town:
          capacity += 1.25;
          break;
        case City:
          capacity -= 2;
          break;
        case Unknown:
          break;
      }
    }
    return (int) capacity;
  }

  /** @return the amount of military support the player requires. */
  public int getSupportRequired()
  {
    double required = 0;
    for ( Army army : _armies )
    {
      required += army.getSupportRequired();
    }
    return (int) required;
  }

  /** @param region improve the embassy in the region by one level. */
  public void improveEmbassy( Region region ) { improveEmbassy( region.getName() ); }

  public void improveEmbassy( String region )
  {
    int level = _embassyLevels.get( region );
    _embassyLevels.put( region, level + 1 );
  }

  private void setEmbassyLevel( Region region, int embassyLevel )
  {
    _embassyLevels.put( region.getName(), embassyLevel );
  }

  /** Reduce the embassy level by one in the region. Can't go below zero. */
  public void degradeEmbassy( Region region )
  {
    int level = _embassyLevels.get( region.getName() );
    _embassyLevels.put( region.getName(), Math.max( 0, level - 1 ) );
  }

  /** The kingdom's pop centers (and other items, if relevant) produce for the turn. */
  public void produceResources( Game game )
  {
    for ( PopCenter popCenter : _popCenters )
    {
      _gold += popCenter.produceResources();
    }

    // Kingdom units generate reinforcements.
    if ( game.getTurn() > 0 )
    {
      int reinforcementsThroughLastTurn = (int) ((game.getTurn() - 1) / _kingdom.getReinforcementRate());
      int reinforcementsThroughThisTurn = (int) (game.getTurn() / _kingdom.getReinforcementRate());
      if ( reinforcementsThroughThisTurn > reinforcementsThroughLastTurn )
      {
        _kingdomTroopsAvailable++;
      }
    }
  }

  /** Pay the kingdom's maintenance (usually just troops). */
  public void payMaintenance()
  {
    double supportPenalty = Math.max( 1.0, getSupportRequired() / getSupportCapacity() );
    for ( Army army : _armies )
    {
      int cost = (int) (army.getMaintenanceCost( _kingdom.getUnitCost() ) * supportPenalty);
      if ( _gold >= cost )
      {
        _gold -= cost;
      }
      else
      {
        // Armies that don't get paid lose morale.
        army.adjustMorale( -10 );
      }
    }
  }

  public void recruitKingdomArmyUnit( Game game, Army army )
  {
    if ( _kingdomTroopsAvailable > 0 )
    {
      _kingdomTroopsAvailable--;
      ArmyUnit unit = _kingdom.createArmyUnit( game.generateUniqueId() );
      army.addUnit( unit );
    }
  }

  public String nextFigureName() { return _kingdom.getFigureName( _nextFigureName++ ); }

  public String nextArmyName()
  {
    String armyNum = Integer.toString( _armies.size() + 1 );
    switch ( armyNum.substring( armyNum.length() - 1 ) )
    {
      case "1":
        return armyNum + (armyNum.equals( "11" ) ? "th " : "st ") + _kingdom.getArmyName();
      case "2":
        return armyNum + (armyNum.equals( "12" ) ? "th " : "nd ") + _kingdom.getArmyName();
      case "3":
        return armyNum + (armyNum.equals( "13" ) ? "th " : "rd ") + _kingdom.getArmyName();
      default:
        return armyNum + "th " + _kingdom.getArmyName();
    }
  }

  /** Initialize the player's embassies in each region to 0 levels. */
  public void setupEmbassies( List<Region> regions )
  {
    for ( Region region : regions )
    {
      _embassyLevels.put( region.getName(), 0 );
    }
  }

  /** @return the player's current level of production. */
  public int getCurrentProduction()
  {
    int total = 0;
    for ( PopCenter popCenter : _popCenters )
    {
      total += popCenter.produceResources();
    }
    return total;
  }

  /** @return the cost of the players armies */
  public int getTroopCost()
  {
    int cost = 0;
    for ( Army army : _armies )
    {
      cost += army.getMaintenanceCost( _kingdom.getUnitCost() );
    }
    return cost;
  }

  /** The embassy levels for each region. */
  @SuppressWarnings("UnusedDeclaration")
  Map<String, Integer> getEmbassyLevels() { return _embassyLevels; }

  public int getEmbassyLevel( Region region ) { return getEmbassyLevel( region.getName() ); }

  public int getEmbassyLevel( String region )
  {
    Integer level = _embassyLevels.get( region );
    return level == null ? 0 : level;
  }

  /** How many kingdom troops are available for recruiting. */
  public int getKingdomTroopsAvailable()
  { return _kingdomTroopsAvailable; }

  public void setKingdomTroopsAvailable( int kingdomTroopsAvailable ) { _kingdomTroopsAvailable = kingdomTroopsAvailable; }


  public boolean equals( Object o )
  {
    return _name.equals( ((Player)o)._name );
  }

  public void spewEquals( Player p )
  {
    LOG.error( "embassy=" + _embassyLevels.equals( p._embassyLevels ) );
    LOG.error( "name=" + _name.equals( p._name ) );
    LOG.error( "capitol=" + _capitol.equals( p._capitol ) );
    LOG.error( "king=" + _king.equals( p._king ) );
    LOG.error( "kingdom=" + _kingdom.equals( p._kingdom ) );
    LOG.error( "_wizards=" + _wizards.equals( p._wizards ) );
    LOG.error( "_diplomats=" + _diplomats.equals( p._diplomats ) );
    LOG.error( "_agents=" + _agents.equals( p._agents ) );
    LOG.error( "_gold=" + (_gold == p._gold) );
    LOG.error( "_power=" + (_power == p._power) );
    LOG.error( "_armies=" + _armies.equals( p._armies ) );
    LOG.error( "_popCenters=" + _popCenters.equals( p._popCenters ) );
    LOG.error( "_orders=" + _turnOrders.equals( p._turnOrders ) );
    LOG.error( "_kingdomTroopsAvailable=" + (_kingdomTroopsAvailable == p._kingdomTroopsAvailable) );
    LOG.error( "_nextFigureName=" + (_nextFigureName == p._nextFigureName) );
    LOG.error( "_view=" + _gameView.equals( p._gameView ) );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this, Arrays.asList( "_jsonCapitolId", "_jsonPopIds", "_needsDeserialization" ) );
  }

  // ------Persistence stuff-------------

  /** Save the player's state and orders to the given directory for the given turn. */
  public void save( File playerDir, int turn ) throws IOException
  {
    try (Writer stateWriter = new BufferedWriter( new FileWriter( new File( playerDir, _name + "_turn" + turn + STATE_FILE ) ) ))
    {
      save( stateWriter );
    }
  }

  void save( Writer out ) throws IOException
  {
    String json = Game.GSON.toJson( this );
    IOUtils.write( json, out );
  }

  /** Meant to load just the player, independent of it's game. Such as when used by a player GUI tool. */
  public static Player load( File playersDir, String playerName, int turn ) throws Exception
  {
    try (Reader stateReader = new BufferedReader( new FileReader( new File( playersDir, playerName + "_turn" + turn + STATE_FILE ) ) ))
    {
      Player player = load( stateReader );
      return player;
    }
  }

  static Player load( Reader stateReader ) throws IOException
  {
    String json = IOUtils.toString( stateReader );
    Player player = Game.GSON.fromJson( json, Player.class );
    return player;
  }

  // ----Stuff related to running a turn---

  /**
   * Update the view for those things the player owns. For example, a player's figures/groups would know the region, terrain, who owns pop
   * center in that locations, etc.
   */
  public void updateIntelligence( Game game )
  {
    addGameStartInfo( game );
    _gameView.setTurn( game.getTurn() );
    updateGeneralIntelForFigures( game );
    updateGeneralIntelForArmies( game );
    updateGeneralIntelForPops( game );
    updateGeneralIntelForControlInfo( game );
  }

  /**
   * Add general game info available to all players such as: - Player ids/names - region ids/names - global features, like cities
   *
   * @param game the overall game.
   */
  public void addGameStartInfo( Game game )
  {
    // Only need to add this information once at the beginning of the game.
    if ( game.getTurn() == 0 )
    {
      if ( _gameView == null )
      {
        _gameView = new Game( game.getName() );
        _gameView._created = true;
        addGameStartMapInfo( game );
        addGameStartPlayerInfo( game.getPlayers() );
        addGameStartCityInfo( game );
        addGameStartMyPops( game );
      }
    }
  }

  private void addGameStartMapInfo( Game game )
  {
    GameMap gameMap = game.getMap();
    GameMap viewMap = GameMap.createBySize( gameMap.getWidth(), gameMap.getHeight() );
    for ( Location viewLocation : viewMap.getLocationsByHex().values() )
    {
      Location mapLocation = gameMap.getLocation( viewLocation.getCoord() );
      viewLocation.setTerrain( mapLocation.getTerrain() );
      viewLocation.setRegion( Region.UNKNOWN_REGION );
    }
    viewMap.setGame( _gameView );
    _gameView.setMap( viewMap );
    addGameStartRegionInfo( gameMap.getRegions() );
  }

  private void addGameStartRegionInfo( List<Region> regions )
  {
    for ( Region region : regions )
    {
      Region regionView = new Region( region.getName() );
      _gameView.getMap().addRegion( regionView );
    }
  }

  private void addGameStartPlayerInfo( List<Player> players )
  {
    for ( Player player : players )
    {
      if ( player.getName().equals( _name ) )
      {
        _gameView.addPlayer( this );
      }
      else
      {
        Player otherPlayer = new Player( player.getName() );
        otherPlayer._kingdom = player._kingdom;
        _gameView.addPlayer( otherPlayer );
      }
    }

    // The player's game view also has various items unowned or owned by someone unknown.
    _gameView.addPlayer( createUnknown() );
    _gameView.addPlayer( createUnowned() );
  }

  private void addGameStartCityInfo( Game game )
  {
    // All players know about cities at the start of the game, though not in much detail.
    List<PopCenter> cities = game.getPopCenters( PopCenter.PopType.City );
    for ( PopCenter city : cities )
    {
      PopCenter intelCity = new PopCenter( city );
      intelCity.setTurnSeen( game.getTurn() );
      intelCity.setLevel( PopCenter.VALUE_UNKNOWN );
      intelCity.setOwner( UNKNOWN );
      addKnownItem( intelCity );
      addKnownLocation( intelCity.getLocation() );
    }
  }

  private void addGameStartMyPops( Game game )
  {
    for ( PopCenter popCenter : _popCenters )
    {
      _gameView.addPopCenter( popCenter );
    }
  }

  /**
   * Update the view's intelligence based on my figures. Figures know:
   * - their location
   * - the pop center if they're using one as a base of
   * operations
   */
  private void updateGeneralIntelForFigures( Game game )
  {
    for ( Figure myFigure : getFigures() )
    {
      GameItem base = myFigure.getBase();
      if ( (base instanceof PopCenter) && !base.getOwnerName().equals( _name ) )
      {
        // Knows general info about the pop center.
        PopCenter intelPop = new PopCenter( (PopCenter) base );
        intelPop.setTurnSeen( game.getTurn() );
        addKnownItem( intelPop );

        // Knows about the location.
        addKnownLocation( base.getLocation() );
      }
      else
      {
        // The player owns the pop already or the figure is in an army and intel is handled by the army.
      }
    }
  }

  /**
   * Update the view's intelligence reports based on my armies. Armies know:
   * - knows the location
   * - any pop owner in the location - any
   * other armies in the location
   */
  private void updateGeneralIntelForArmies( Game game )
  {
    for ( Army myArmy : _armies )
    {
      // Knows about the location.
      Location armyLocation = myArmy.getLocation();
      addKnownLocation( armyLocation );

      // Knows about any pop center at that location.
      PopCenter pop = game.getPopCenter( armyLocation.getCoord() );
      if ( (pop != null) && !pop.getOwnerName().equals( _name ) )
      {
        // Knows full info about the pop center (though not who might be in it).
        PopCenter intelPop = new PopCenter( pop );
        intelPop.setTurnSeen( game.getTurn() );
        addKnownItem( intelPop );
      }

      // Knows about other armies in that location.
      for ( Army otherArmy : game.getAllPlayerArmies() )
      {
        updateIntelForArmyInMyLocation( game, myArmy, otherArmy );
      }
    }
  }

  /** Update the view's intelligence based on my pop centers. Pops know:
   * - the location
   * - any armies at that location */
  private void updateGeneralIntelForPops( Game game )
  {
    // Pops know their location, surrounding locations, and any armies sitting on that location.
    for ( PopCenter myPop : _popCenters )
    {
      for ( Tuple tuple : HexCalculator.getAllNeighbors( myPop.getLocation().getCoord(), 1 ) )
      {
        Location location = game.getMap().getLocationsByHex().get( tuple );
        if ( location != null )
        {
          addKnownLocation( location );
        }
      }

      // Knows about other armies in that location.
      for ( Army otherArmy : game.getAllPlayerArmies() )
      {
        updateIntelForArmyInMyLocation( game, myPop, otherArmy );
      }
    }
  }

  /** Players know who is in control of each region. They also know their control level in every region. */
  private void updateGeneralIntelForControlInfo( Game game )
  {
    // Know who controls each region.
    Map<Region, Player> generalControlInfo = game.getRegionOwners();
    for ( Map.Entry<Region, Player> regionPlayerEntry : generalControlInfo.entrySet() )
    {
      Player player = regionPlayerEntry.getValue();
      Region region = regionPlayerEntry.getKey();
      if ( !player.equals( UNOWNED ) && !player.equals( UNKNOWN ) )
      {
        addKnownControlLevel( region, player, region.getControlLevel( player ) );
      }
    }

    // Know my control level in each region.
    for ( Region region : game.getMap().getRegions() )
    {
      ControlLevel controlLevel = getControlLevel( region );
      addKnownControlLevel( region, this, controlLevel );
    }

    hearAboutBigArmies( game );
  }

  /** Hear about any sizable groups. */
  private void hearAboutBigArmies( Game game )
  {
    List<Player> otherPlayers = new ArrayList<>( game.getPlayers() );
    otherPlayers.remove( this );
    for ( Player player : otherPlayers )
    {
      for ( Army army : player.getArmies() )
      {
        int noticeLevel = 100;
        Region region = army.getLocation().getRegion();
        switch ( getControlLevel( region ) )
        {
          case None:
            noticeLevel = 20;
            break;
          case Presence:
            noticeLevel = 10;
            break;
          case Control:
            noticeLevel = 4;
            break;
          case Domination:
            noticeLevel = 2;
            break;
          case Unknown:
            noticeLevel = 100;
            break;
        }

        if ( (army.getUnits().size() >= noticeLevel) && !army.isInvisible() )
        {
          GameEvent event = new GameEvent(
            army.getName() + " of " + army.getOwner().getName() + " has been spotted in " + region.getName(),
            Location.NOWHERE, game.getTurn() );
          addGameEvent( event );
        }
      }
    }
  }

  private void updateIntelForArmyInMyLocation( Game game, GameItem myItem, Army otherArmy )
  {
    boolean isAnotherPlayerArmy = !otherArmy.getOwnerName().equals( _name );
    boolean isAtMyArmyLocation = otherArmy.getLocation().equals( myItem.getLocation() );
    if ( isAnotherPlayerArmy && isAtMyArmyLocation && !otherArmy.isInvisible() )
    {
      Army intelArmy = new Army( otherArmy );
      intelArmy.clearUnits();
      intelArmy.setTurnSeen( game.getTurn() );
      for ( int i = 0; i < otherArmy.getUnits().size(); i++ )
      {
        intelArmy.addUnit( ArmyUnit.UNKNOWN_UNIT );
      }
      addKnownItem( intelArmy );
    }
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  /** Add the item, replacing any older knowledge of the item. */
  public void addKnownItem( GameItem item )
  {
    if ( _gameView != null ) // Non-players (e.g. UNKNOWN) have no game view.
    {
      Player ownerView = _gameView.getPlayer( item.getOwnerName() );
      if ( item instanceof Figure )
      {
        if ( item instanceof King )
        {
          ownerView.setKing( (King) item );
        }
        else
        {
          Figure figure = ownerView.getFigure( item.getId() );
          if ( figure == null )
          {
            ownerView.add( (Figure)item );
          }
          else if ( figure.getTurnSeen() < item.getTurnSeen() )
          {
            ownerView.remove( (Figure)item );
            ownerView.add( (Figure)item );
          }
        }
      }
      else if ( item instanceof PopCenter )
      {
        _gameView.addPopCenter( (PopCenter) item );
        ownerView.remove( (PopCenter) item );
        ownerView.add( (PopCenter) item );
      }
      else if ( item instanceof Army )
      {
        Army oldArmy = ownerView.getArmy( item.getId() );
        if ( oldArmy == null )
        {
          ownerView.add( (Army) item );
        }
        else if ( oldArmy.getTurnSeen() < item.getTurnSeen() )
        {
          ownerView.remove( (Army) item );
          ownerView.add( (Army) item );
        }
      }
      else
      {
        throw new RuntimeException( "Invalid item type to add: " + item.getClass() );
      }
    }
  }

  public void addKnownLocation( Location loc )
  {
    if ( _gameView != null ) // Non-players (e.g. UNKNOWN) have no game view.
    {
      Location viewLocation = _gameView.getMap().getLocation( loc.getCoord() );
      Region newViewRegion = _gameView.getMap().getRegion( loc.getRegion().getName() );
      Region oldViewRegion = viewLocation.getRegion();
      oldViewRegion.removeLocation( viewLocation );
      viewLocation.setRegion( newViewRegion );
      newViewRegion.addLocation( viewLocation );
    }
  }

  public void addKnownEmbassyLevel( Region region, Player player, int embassyLevel )
  {
    if ( _gameView != null ) // Non-players (e.g. UNKNOWN) have no game view.
    {
      Player viewPlayer = _gameView.getPlayer( player.getName() );
      viewPlayer.setEmbassyLevel( region, embassyLevel );
    }
  }
  public int getKnownEmbassyLevel( Region region, Player player )
  {
    return _gameView.getEmbassyLevel( region, player );
  }
  public Map<Player, Integer> getKnownEmbassyLevels( Region region )
  {
    return _gameView.getEmbassyLevels( region );
  }

  public void addKnownControlLevel( Region region, Player player, ControlLevel controlLevel )
  {
    Map<String, ControlLevel> controlInfo = _controlLevelIntel.get( player.getName() );
    if ( controlInfo == null )
    {
      controlInfo = new HashMap<>();
      _controlLevelIntel.put( player.getName(), controlInfo );
    }
    controlInfo.put( region.getName(), controlLevel );
  }

  /** There are times where a player knows an item is no longer on the board (such as a dead agent). */
  @SuppressWarnings("ChainOfInstanceofChecks")
  public void removeKnownItem( GameItem item )
  {
    List<Player> players = new ArrayList<>( _gameView.getPlayers() );
    players.remove( this );
    for ( Player player : players )
    {
      if ( item instanceof Figure )
      {
        player.remove( (Figure) item );
      }
      else if ( item instanceof PopCenter )
      {
        player.remove( (PopCenter) item );
      }
      else if ( item instanceof Army )
      {
        player.remove( (Army) item );
      }
      else
      {
        throw new RuntimeException( "Invalid item type to remove: " + item.getClass() );
      }
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public ControlLevel getControlLevelIntel( Region region, Player player )
  {
    return getControlLevelIntel( region, player.getName() );
  }

  public ControlLevel getControlLevelIntel( Region region, String playerName )
  {
    Map<String, ControlLevel> controlInfo = _controlLevelIntel.get( playerName );
    if ( controlInfo != null )
    {
      ControlLevel level = controlInfo.get( region.getName() );
      if ( level != null )
      {
        return level;
      }
    }
    return ControlLevel.Unknown;
  }

  public static class MyJsonAdapter implements JsonSerializer<Player>, JsonDeserializer<Player>
  {
    // Painful, but we have to manually serialize the bits so we can handle a special case with _gameView to avoid
    // circular reference in serialization.
    private static final String NAME = "name";
    private static final String KINGDOM = "kingdom";
    private static final String CAPITOL_ID = "capitol";
    private static final String KING = "king";
    private static final String WIZARDS = "wizards";
    private static final String DIPLOMATS = "diplomats";
    private static final String AGENTS = "agents";
    private static final String ARMIES = "armies";
    private static final String POP_IDS = "popIds";
    private static final String GOLD = "gold";
    private static final String POWER = "power";
    private static final String KINGDOM_TROOPS_AVAILABLE = "kingdomTroopsAvailable";
    private static final String NEXT_FIGURE_NAME = "nextFigureName";
    private static final String EMBASSIES = "embassies";
    private static final String ORDERS = "orders";
    private static final String EVENTS = "events";
    private static final String CONTROL_INTEL = "controlIntel";
    private static final String GAME_VIEW = "gameView";

    public JsonElement serialize( Player player, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      jsonResponse.add( NAME, context.serialize( player._name ) );
      if ( player._kingdom != null ) { jsonResponse.add( KINGDOM, context.serialize( player._kingdom.getName() ) ); }
      if ( player._capitol != null ) { jsonResponse.add( CAPITOL_ID, context.serialize( player._capitol.getId() ) ); }
      if ( player._king != null ) jsonResponse.add( KING, context.serialize( player._king ) );
      if ( player._wizards != null ) jsonResponse.add( WIZARDS, context.serialize( player._wizards ) );
      if ( player._diplomats != null ) jsonResponse.add( DIPLOMATS, context.serialize( player._diplomats ) );
      if ( player._agents != null ) jsonResponse.add( AGENTS, context.serialize( player._agents ) );
      if ( player._armies != null ) jsonResponse.add( ARMIES, context.serialize( player._armies ) );
      if ( player._popCenters != null )
      {
        List<Integer> popIds = new ArrayList<>();
        for ( PopCenter popCenter : player._popCenters )
        {
          popIds.add( popCenter.getId() );
        }
        jsonResponse.add( POP_IDS, context.serialize( popIds ) );
      }
      jsonResponse.add( GOLD, context.serialize( player._gold ) );
      jsonResponse.add( POWER, context.serialize( player._power ) );
      jsonResponse.add( KINGDOM_TROOPS_AVAILABLE, context.serialize( player._kingdomTroopsAvailable ) );
      jsonResponse.add( NEXT_FIGURE_NAME, context.serialize( player._nextFigureName ) );
      if ( player._embassyLevels != null ) { jsonResponse.add( EMBASSIES, context.serialize( player._embassyLevels ) ); }
      if ( player._turnOrders != null ) { jsonResponse.add( ORDERS, context.serialize( player._turnOrders ) ); }
      if ( player._events != null ) { jsonResponse.add( EVENTS, context.serialize( player._events ) ); }
      if ( player._controlLevelIntel != null ) { jsonResponse.add( CONTROL_INTEL, context.serialize( player._controlLevelIntel ) ); }
      if ( player._gameView != null )
      {
        // Don't serialize the player state within their own game view. It will cause a circular serialization.
        player._gameView.getPlayers().remove( player );
        jsonResponse.add( GAME_VIEW, context.serialize( player._gameView ) );
        player._gameView.getPlayers().add( player );
      }
      return jsonResponse;
    }

    public Player deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Player player = new Player();
      JsonObject asJsonObject = jsonOrder.getAsJsonObject();
      player._name = context.deserialize( asJsonObject.get( NAME ), String.class );
      String kingdom = context.deserialize( asJsonObject.get( KINGDOM ), String.class );
      if ( kingdom != null ) { player._kingdom = Kingdoms.KINGDOMS.get( kingdom ); }
      player._jsonCapitolId = context.deserialize( asJsonObject.get( CAPITOL_ID ), Integer.class );
      player._king = context.deserialize( asJsonObject.get( KING ), King.class );
      player._wizards = context.deserialize( asJsonObject.get( WIZARDS ), new TypeToken<List<Wizard>>(){}.getType() );
      player._diplomats = context.deserialize( asJsonObject.get( DIPLOMATS ), new TypeToken<List<Diplomat>>(){}.getType() );
      player._agents = context.deserialize( asJsonObject.get( AGENTS ), new TypeToken<List<Agent>>(){}.getType() );
      player._armies = context.deserialize( asJsonObject.get( ARMIES ), new TypeToken<List<Army>>(){}.getType() );
      player._jsonPopIds = context.deserialize( asJsonObject.get( POP_IDS ), new TypeToken<List<Integer>>(){}.getType() );
      player._gold = context.deserialize( asJsonObject.get( GOLD ), Integer.class );
      player._power = context.deserialize( asJsonObject.get( POWER ), Integer.class );
      player._kingdomTroopsAvailable = context.deserialize( asJsonObject.get( KINGDOM_TROOPS_AVAILABLE ), Integer.class );
      player._nextFigureName = context.deserialize( asJsonObject.get( NEXT_FIGURE_NAME ), Integer.class );
      player._embassyLevels = context.deserialize( asJsonObject.get( EMBASSIES ), new TypeToken<Map<String, Integer>>(){}.getType() );
      player._events = context.deserialize( asJsonObject.get( EVENTS ), new TypeToken<List<GameEvent>>(){}.getType() );
      player._controlLevelIntel = context.deserialize( asJsonObject.get( CONTROL_INTEL ), new TypeToken<Map<String, Map<String, ControlLevel>>>(){}.getType() );
      JsonElement gameViewElement = asJsonObject.get( GAME_VIEW );
      if ( gameViewElement != null )
      {
        player._gameView = context.deserialize( gameViewElement, Game.class );
        player._gameView.addPlayer( player );
      }
      player._turnOrders = context.deserialize( asJsonObject.get( ORDERS ), new TypeToken<List<Order>>(){}.getType() );

      return player;
    }
  }

  /** Fix all the players references (locations, pops, etc). */
  public void fixDeserializationReferences()
  {
    _gameView.fixDeserializationReferences();
  }

  boolean _needsDeserialization = true;
  public void fixDeserializationReferences( Game game )
  {
    if ( _needsDeserialization )
    {
      _needsDeserialization = false;
      fixPopCenterDeserializationReferences( game );
      fixCapitolDeserializationReference();
      fixFiguresDeserializationReferences( game );
      fixArmyDeserializationReferences( game );
      if ( _gameView != null ) { _gameView.fixDeserializationReferences(); }
    }
  }

  private void fixPopCenterDeserializationReferences( Game game )
  {
    for ( Integer popId : _jsonPopIds )
    {
      PopCenter pop = game.getPopCenter( popId );
      if ( pop != null )
      {
        pop.fixDeserializationReferences( game );
        _popCenters.add( pop );
      }
      else
      {
        throw new RuntimeException( "No pop with id: " + popId );
      }
    }
  }

  private void fixCapitolDeserializationReference()
  {
    if ( _jsonCapitolId != null )
    {
      for ( PopCenter popCenter : _popCenters )
      {
        if ( popCenter.getId() == _jsonCapitolId )
        {
          _capitol = popCenter;
        }
      }
    }
  }

  private void fixFiguresDeserializationReferences( Game game )
  {
    for ( Figure figure : getFigures() )
    {
      figure.fixDeserializationReferences( game );
    }
  }

  private void fixArmyDeserializationReferences( Game game )
  {
    for ( Army army : _armies )
    {
      army.fixDeserializationReferences( game );
    }
  }

  /**
   * Must be called after the rest of the entire Game is deserialization fixed since orders frequently reference game items
   * outside of the player's ownership.
   */
  public void fixTurnOrdersDeserialization( Game game )
  {
    for ( Order turnOrder : _turnOrders )
    {
      turnOrder.fixDeserializationReferences( game );
    }
  }
}