package kessel.hex.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kessel.hex.domain.PopCenter.PopType;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.GameMap;
import kessel.hex.map.Location;
import kessel.hex.map.MapCreator;
import kessel.hex.map.Region;
import kessel.hex.orders.Order;
import kessel.hex.orders.agent.AgentReconLocation;
import kessel.hex.orders.agent.AgentScoutControlLevel;
import kessel.hex.orders.agent.AgentScoutEmbassies;
import kessel.hex.orders.agent.AssassinateAgent;
import kessel.hex.orders.agent.AssassinateDiplomat;
import kessel.hex.orders.agent.AssassinateWizard;
import kessel.hex.orders.agent.CounterEspionage;
import kessel.hex.orders.agent.HireAgent;
import kessel.hex.orders.agent.MoveAgent;
import kessel.hex.orders.agent.SabotageEmbassy;
import kessel.hex.orders.agent.SabotagePopCenter;
import kessel.hex.orders.agent.TrainAgent;
import kessel.hex.orders.army.ArmyAttackArmy;
import kessel.hex.orders.army.ArmyAttackPop;
import kessel.hex.orders.army.ArmyCombine;
import kessel.hex.orders.army.ArmyMove;
import kessel.hex.orders.army.ArmyRecruit;
import kessel.hex.orders.army.ArmySearch;
import kessel.hex.orders.army.ArmyTransfer;
import kessel.hex.orders.army.CreateArmy;
import kessel.hex.orders.army.DemandSurrender;
import kessel.hex.orders.army.RetireUnit;
import kessel.hex.orders.diplomat.DiplomatInciteRebellion;
import kessel.hex.orders.diplomat.DiplomatInspireLoyalty;
import kessel.hex.orders.diplomat.DiplomatNegotiateFealty;
import kessel.hex.orders.diplomat.HireDiplomat;
import kessel.hex.orders.diplomat.MapRegion;
import kessel.hex.orders.diplomat.MoveDiplomat;
import kessel.hex.orders.diplomat.TakeRegionCensus;
import kessel.hex.orders.diplomat.TrainDiplomat;
import kessel.hex.orders.king.ImproveEmbassy;
import kessel.hex.orders.king.ImprovePopCenter;
import kessel.hex.orders.king.ImprovePower;
import kessel.hex.orders.king.MoveCapitol;
import kessel.hex.orders.wizard.Alchemy;
import kessel.hex.orders.wizard.BadOmen;
import kessel.hex.orders.wizard.CharmFigure;
import kessel.hex.orders.wizard.CharmRegion;
import kessel.hex.orders.wizard.CorruptEmbassy;
import kessel.hex.orders.wizard.CurseRegion;
import kessel.hex.orders.wizard.DamagePopCenter;
import kessel.hex.orders.wizard.DamageRegion;
import kessel.hex.orders.wizard.DarkRitual;
import kessel.hex.orders.wizard.DeepFog;
import kessel.hex.orders.wizard.DiplomaticAura;
import kessel.hex.orders.wizard.DispelCombatMagic;
import kessel.hex.orders.wizard.EnhanceEmbassy;
import kessel.hex.orders.wizard.EnhancePower;
import kessel.hex.orders.wizard.EnhancedInvisibleArmy;
import kessel.hex.orders.wizard.EnhancedTeleportArmy;
import kessel.hex.orders.wizard.FireStorm;
import kessel.hex.orders.wizard.Fireball;
import kessel.hex.orders.wizard.HireWizard;
import kessel.hex.orders.wizard.ImbuePopCenter;
import kessel.hex.orders.wizard.ImbueRegion;
import kessel.hex.orders.wizard.InvisibleArmy;
import kessel.hex.orders.wizard.MagicDome;
import kessel.hex.orders.wizard.MoveWizard;
import kessel.hex.orders.wizard.PhantomTroops;
import kessel.hex.orders.wizard.PlagueArmy;
import kessel.hex.orders.wizard.RaiseDead;
import kessel.hex.orders.wizard.SummonDragons;
import kessel.hex.orders.wizard.SummonOgres;
import kessel.hex.orders.wizard.SummonWerebeasts;
import kessel.hex.orders.wizard.Scry;
import kessel.hex.orders.wizard.ShadowAssassin;
import kessel.hex.orders.wizard.ShieldFigure;
import kessel.hex.orders.wizard.ShieldRegion;
import kessel.hex.orders.wizard.SubvertCity;
import kessel.hex.orders.wizard.SubvertHamlet;
import kessel.hex.orders.wizard.SubvertTown;
import kessel.hex.orders.wizard.TeleportArmy;
import kessel.hex.orders.wizard.TeleportFigure;
import kessel.hex.orders.wizard.TeleportSelf;
import kessel.hex.orders.wizard.TrainWizard;
import kessel.hex.orders.wizard.UnlimitedInvisibleArmy;
import kessel.hex.orders.wizard.UnlimitedTeleportArmy;
import kessel.hex.util.AtomicIntegerJsonAdapter;
import kessel.hex.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Creates the Game. This includes creating the map, the towns, the regions, etc. */
@SuppressWarnings({ "PackageVisibleField", "RawUseOfParameterizedType" })
public class Game
{
  private static final Logger LOG = Logger.getLogger( Game.class );

  // Everything in the game costs or produces at some modification of this universal modifier. That way, if there's
  // ever a desire to add/remove granularity (such as production/cost going from 10's of gold to 1000's of gold)
  // then this is the only value that needs to change.
  public static final int GOLD_GRANULARITY = 100;

  // Standard starting levels for population centers. All costs are balanced around how many "Hamlets" worth
  // of production it should require.
  public static final int BASE_HAMLET_PRODUCTION = 3;
  public static final int BASE_TOWN_PRODUCTION = 7;
  public static final int BASE_CITY_PRODUCTION = 14;

  public static final String GAME_FILE = "game.json";
  public static final String ADMIN_DIR = "admin";
  public static final String PLAYERS_DIR = "player";

  // The single GSON instance used in _all_ serialization.
  public static final Gson GSON = createGson();

  /** The name of the game, which must be unique among all games. */
  private String _name;

  // All the pop centers, conveniently broken out by type.
  final Map<PopType, Map<Tuple, PopCenter>> _popCenters = new EnumMap<>( PopType.class );

  {
    for ( PopType popCenterType : PopType.values() )
    {
      _popCenters.put( popCenterType, new HashMap<Tuple, PopCenter>() );
    }
  }

  List<Player> _players = new ArrayList<>();
  GameMap _map;
  private int _turn;
  private final AtomicInteger _idGenerator = new AtomicInteger( 1 );
  private final transient GameTurn _currentTurn = new GameTurn();
  boolean _created = false;

  private transient MapCreator.MapCreationListener _listener;

  // Used only by GSON
  public Game() {}
  public Game( String name )
  {
    _name = name;
  }

  /** Executes the actual game construction: map creation, player positions, etc. */
  public void createGame( int numPlayers, int hexesPerPlayer, double regionsPerPlayer, int townsPerPlayer )
  {
    if ( LOG.isDebugEnabled() ) { LOG.debug( "Construct game started" ); }

    _players = new PlayerCreator( this ).createPlayers( numPlayers, true );
    _map = new MapCreator( this, _listener ).createMap( numPlayers, hexesPerPlayer, regionsPerPlayer, townsPerPlayer );
    updateIntelligence();
    _created = true;

    if ( LOG.isDebugEnabled() )
    {
      LOG.debug( "Construct game finished" );
      LOG.debug( "Players = " + getPlayerNames() );
      LOG.debug( "Map: " + _map.debugInfo() );
    }
  }

  public void addPlayer( Player player ) { _players.add( player ); }

  public List<Player> getPlayers() { return _players; }

  public Player getPlayer( String playerName )
  {
    for ( Player player : _players )
    {
      if ( player.getName().equals( playerName ) )
      {
        return player;
      }
    }
    return null;
  }

  public List<String> getPlayerNames()
  {
    List<String> names = new ArrayList<>();
    for ( Player player : _players )
    {
      names.add( player.getName() );
    }
    return names;
  }


  /** Get all the pop centers owned by players. */
  public List<PopCenter> getAllPlayerCapitols()
  {
    List<PopCenter> pops = new ArrayList<>();
    for ( Player player : _players )
    {
      pops.add( player.getCapitol() );
    }
    return pops;
  }

  /** Get all the armies owned by players. */
  public List<Army> getAllPlayerArmies()
  {
    List<Army> armies = new ArrayList<>();
    for ( Player player : _players )
    {
      armies.addAll( player.getArmies() );
    }
    return armies;
  }

  public List<Figure> getAllPlayerFigures()
  {
    List<Figure> figures = new ArrayList<>();
    for ( Player player : _players )
    {
      List<Figure> figures1 = player.getFigures();
      figures.addAll( figures1 );
    }
    return figures;
  }

  public GameMap getMap() { return _map; }

  public void setMap( GameMap map ) { _map = map; }

  /** @return the pop center at the given hex or null if there isn't one. */
  public PopCenter getPopCenter( Location location )
  { return getPopCenter( location.getCoord() ); }

  public PopCenter getPopCenter( int x, int y ) { return getPopCenter( new Tuple( x, y ) ); }

  public PopCenter getPopCenter( Tuple hex )
  {
    for ( PopType value : PopType.values() )
    {
      PopCenter pop = _popCenters.get( value ).get( hex );
      if ( pop != null )
      {
        return pop;
      }
    }
    return null;
  }

  /** @return the pop center with the given unique id or null if not found. */
  public PopCenter getPopCenter( int popCenterId )
  {
    for ( Map<Tuple, PopCenter> popsByType : _popCenters.values() )
    {
      for ( PopCenter popCenter : popsByType.values() )
      {
        if ( popCenter.getId() == popCenterId )
        {
          return popCenter;
        }
      }
    }
    return null;
  }

  public void addPopCenter( PopCenter pop )
  {
    _popCenters.get( pop.getType() ).put( pop.getLocation().getCoord(), pop );
    _map.getRegion( pop.getLocation().getRegion().getName() ).addPopCenter( pop );
  }

  /** @return all pop centers of the given type. */
  public List<PopCenter> getPopCenters( PopType popCenterType )
  {
    return new ArrayList<>( _popCenters.get( popCenterType ).values() );
  }

  /** @return all pop centers. */
  public List<PopCenter> getAllPopCenters()
  {
    List<PopCenter> pops = new ArrayList<>();
    for ( PopType value : PopType.values() )
    {
      pops.addAll( _popCenters.get( value ).values() );
    }
    return pops;
  }

  /** Get every item in the game. */
  public List<GameItem> getAllGameItems()
  {
    List<GameItem> items = new ArrayList<>();
    items.addAll( getAllPopCenters() );
    items.addAll( getAllPlayerArmies() );
    items.addAll( getAllPlayerFigures() );
    return items;
  }

  /** Get every item in the game in the given hex. */
  public List<GameItem> getAllGameItems( Tuple coord )
  {
    List<GameItem> items = new ArrayList<>();
    for ( GameItem item : getAllGameItems() )
    {
      if ( item.getLocation().getCoord().equals( coord ) )
      {
        items.add( item );
      }
    }
    return items;
  }

  /** @return the Army with the given unique id or null if not found. */
  @SuppressWarnings({ "UnusedDeclaration" })
  public Army getArmy( int armyId )
  {
    for ( Player player : _players )
    {
      Army army = player.getArmy( armyId );
      if ( army != null )
      {
        return army;
      }
    }
    return null;
  }

  /** @return the game item associated with the unique id. */
  public GameItem getItem( int id )
  {
    // It's either a pop center on the map or something owned by a player.
    GameItem item = getPopCenter( id );
    if ( item == null )
    {
      for ( Player player : _players )
      {
        item = player.getItem( id );
        if ( item != null ) { break; }
      }
    }
    return item;
  }

  public String getName() { return _name; }

  public int getTurn() { return _turn; }

  public void setTurn( int turn ) { _turn = turn; }

  public void incrementTurn() { _turn++; }

  public GameTurn getCurrentTurn() { return _currentTurn; }

  /** Save the game and all player output files to the given directory. */
  @SuppressWarnings({ "ResultOfMethodCallIgnored" })
  public void save( File gameDir ) throws IOException
  {
    File adminDir = new File( gameDir, ADMIN_DIR );
    adminDir.mkdir();
    File playersDir = new File( gameDir, PLAYERS_DIR );
    playersDir.mkdir();
    try (Writer w = new BufferedWriter( new FileWriter( new File( adminDir, GAME_FILE ) ) ))
    {
      save( w );
      savePlayers( playersDir, _turn );
    }
  }

  private void savePlayers( File playersDir, int turn ) throws IOException
  {
    for ( Player player : _players )
    {
      player.save( playersDir, turn );
    }
  }

  void save( Writer out ) throws IOException
  {
    String json = GSON.toJson( this );
    IOUtils.write( json, out );
  }

  /** Load a game from disk. */
  public static Game load( File gameDir )
  {
    try (Reader in = new BufferedReader( new FileReader( new File( new File( gameDir, ADMIN_DIR ), GAME_FILE ) ) ))
    {
      Game game = load( in );
      return game;
    }
    catch ( IOException e )
    {
      throw new RuntimeException( e );
    }
  }

  static Game load( Reader in )
  {
    String json = null;
    try
    {
      json = IOUtils.toString( in );
    }
    catch ( IOException e )
    {
      throw new RuntimeException( e );
    }
    Game game = GSON.fromJson( json, Game.class );
    game.fixDeserializationReferences();
    return game;
  }

  /**
   * Run a game turn. This involves:
   * - loading the game from disk
   * - finding the player orders files
   * - validating all order files are present
   * - executing the orders
   *
   * This does _NOT_ save the game. It's up to the client running the turn to decide if the results should be persisted.
   *
   * @param gameDir the directory containing the game.
   * @return the game after the turn has been run.
   */
  public static Game runTurn( File gameDir )
  {
    Game game = load( gameDir );
    game.doRunTurn( gameDir );
    return game;
  }

  private void doRunTurn( File gameDir )
  {
    incrementTurn();
    List<Order> orders = loadPlayerOrders( new File( gameDir, PLAYERS_DIR ), this );
    processOrders( orders );
    checkForRegionalControlChanges();
    produceResources();
    payMaintenance();
    updateIntelligence();
  }

  /** Execute all of the given orders. */
  public void processOrders( List<Order> orders )
  {
    // First, clear all the lastTurnStatus info for every game item before we run the orders for the new turn.
    for ( GameItem item : getAllGameItems() )
    {
      item.clearLastTurnStatus();
    }

    // Remember the control level before orders are executed.
    Map<Region, Player> controlInfo = getRegionOwners();
    _currentTurn.setRegionalControlInfo( controlInfo );

    // Shuffle the orders, then sort so that orders of the same type will be randomly ordered after the sort.
    Collections.shuffle( orders );
    Collections.sort( orders, OrderComparator.INSTANCE );
    for ( Order order : orders )
    {
      order.execute( this );
    }
  }

  /** Look at every region for every player and take any actions related to changes of control. */
  private void checkForRegionalControlChanges()
  {
    Map<Region, Player> previousControlInfo = _currentTurn.getRegionalControlInfo();
    Map<Region, Player> newControlInfo = getRegionOwners();
    for ( Region region : newControlInfo.keySet() )
    {
      Player oldOwner = previousControlInfo.get( region );
      Player newOwner = newControlInfo.get( region );
      if ( !oldOwner.equals( newOwner ) )
      {
        // The old owner loses an embassy level.
        oldOwner.degradeEmbassy( region );
        GameEvent oldOwnerEvent = new GameEvent(
          "The populace of " + region.getName() + "defaces your embassy in celebration of their liberation.",
          Location.NOWHERE, _turn );
        oldOwner.addGameEvent( oldOwnerEvent );

        // The new owner gains an embassy level and learns of all pops in the region.
        newOwner.improveEmbassy( region );
        GameEvent newOwnerEvent = new GameEvent(
          "The populace of " + region.getName() + " enhances your embassy in celebration of your glorious rule.",
          Location.NOWHERE, _turn );
        newOwner.addGameEvent( newOwnerEvent );
        region.gainRegionalPopKnowledge( newOwner, Arrays.asList( PopType.values() ), _turn );
      }
    }
  }

  /** @return which player controls each region. Uncontrolled regions are reported as owned by Player.UNOWNED. */
  public Map<Region, Player> getRegionOwners()
  {
    Map<Region, Player> controlInfo = new HashMap<>();
    for ( Region region : _map.getRegions() )
    {
      controlInfo.put( region, Player.UNOWNED );
      for ( Player player : _players )
      {
        ControlLevel playerControlLevel = region.getControlLevel( player );
        if ( (playerControlLevel == ControlLevel.Control) ||
             (playerControlLevel == ControlLevel.Domination) )
        {
          controlInfo.put( region, player );
        }
      }
    }
    return controlInfo;
  }

  /** @return a player's control level in the given region. */
  public ControlLevel getControlLevel( Region region, String playerName )
  {
    return getPlayer( playerName ).getControlLevel( region );
  }

  /** @return a player's embassy level in the given region. */
  public int getEmbassyLevel( Region region, String playerName )
  {
    return getPlayer( playerName ).getEmbassyLevel( region );
  }
  public int getEmbassyLevel( Region region, Player player )
  {
    return getEmbassyLevel( region, player.getName() );
  }
  public Map<Player, Integer> getEmbassyLevels( Region region )
  {
    Map<Player,Integer> levels = new HashMap<>();
    for ( Player player : _players )
    {
      if ( !player.equals( Player.UNKNOWN ) &&
           !player.equals( Player.UNOWNED ) )
      {
        levels.put( player, player.getEmbassyLevel( region ) );
      }
    }
    return levels;
  }

  /** Execute game production. */
  void produceResources()
  {
    for ( Player player : _players )
    {
      player.produceResources( this );
    }
  }

  /** Handle maintenance for each player. */
  private void payMaintenance()
  {
    for ( Player player : _players )
    {
      player.payMaintenance();
    }
  }

  public void updateIntelligence()
  {
    for ( Player player : getPlayers() )
    {
      player.updateIntelligence( this );
    }
  }

  private static List<Order> loadPlayerOrders( File playersDir, Game game ) throws OrderLoadException
  {
    List<Order> orders = new ArrayList<>();
    boolean success = true;
    for ( Player player : game.getPlayers() )
    {
      try
      {
        Player tmpPlayer = Player.load( playersDir, player.getName(), game.getTurn() - 1 );
        tmpPlayer.fixTurnOrdersDeserialization( game );
        if ( tmpPlayer.getNextTurnOrders().isEmpty() )
        {
          LOG.error( "Player " + tmpPlayer.getName() + " has issued no orders" );
        }
        orders.addAll( tmpPlayer.getNextTurnOrders() );
      }
      catch ( Exception e )
      {
        LOG.debug( "Failure loading orders", e );
        success = false;
      }
    }
    if ( !success )
    {
      throw new OrderLoadException();
    }
    return orders;
  }

  /** Return a unique id for an object in the game. */
  public int generateUniqueId()
  {
    return _idGenerator.getAndIncrement();
  }

  public boolean equals( Object o )
  {
    return EqualsBuilder.reflectionEquals( this, o, Arrays.asList( "_idGenerator" ) ) &&
           _idGenerator.get() == ((Game) o)._idGenerator.get();
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this, Arrays.asList( "_idGenerator" ) );
  }

  public boolean isCreated()
  {
    return _created;
  }

  /** This exists just to support a listener to make game construction visualization easier in a GUI. */
  public void setCreationListener( MapCreator.MapCreationListener listener ) { _listener = listener; }

  /** Fix all references after deserialization: e.g. figure's bases, game item locations, etc. */
  public void fixDeserializationReferences()
  {
    fixMapDeserializationReferences();
    fixPopCenterDeserializationReferences();
    fixPlayersDeserializationReferences();
    fixPlayersOrdersDeserializationReferences();
  }

  private void fixMapDeserializationReferences()
  {
    _map.fixDeserializationReferences( this );
  }

  private void fixPopCenterDeserializationReferences()
  {
    for ( Map<Tuple, PopCenter> tuplePopCenterMap : _popCenters.values() )
    {
      for ( PopCenter popCenter : tuplePopCenterMap.values() )
      {
        popCenter.fixDeserializationReferences( this );
      }
    }
  }

  private void fixPlayersDeserializationReferences()
  {
    for ( Player player : _players )
    {
      player.fixDeserializationReferences( this );
    }
  }

  private void fixPlayersOrdersDeserializationReferences()
  {
    for ( Player player : _players )
    {
      player.fixTurnOrdersDeserialization( this );
    }
  }

  /** Thrown when there's a failure loading player orders. */
  private static class OrderLoadException extends RuntimeException
  {
  }

  private static class OrderComparator implements Comparator<Order>
  {
    static final Map<String, Integer> _orderSequence = new HashMap<>();

    static
    {
      try (InputStream inStream = Order.class.getResourceAsStream( "./orderSequence.json" ))
      {
        String json = StringUtils.join( IOUtils.readLines( inStream ).toArray() );
        List<String> orderSequence = GSON.fromJson( json, new TypeToken<List<String>>(){}.getType() );
        int i = 0;
        for ( String orderName : orderSequence )
        {
          _orderSequence.put( orderName, i );
          i++;
        }
      }
      catch ( IOException e )
      {
        throw new RuntimeException( "Failed to load order sequencing file: orderSequence.json", e );
      }
    }

    static final OrderComparator INSTANCE = new OrderComparator();

    public int compare( Order a, Order b )
    {
      Integer aVal = _orderSequence.get( a.getClass().getSimpleName() );
      Integer bVal = _orderSequence.get( b.getClass().getSimpleName() );
      return aVal.compareTo( bVal );
    }
  }

  // Create the GSON instance. There are tons of specialized adapters for all the various orders in the game.
  private static Gson createGson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.enableComplexMapKeySerialization();
    builder.registerTypeAdapter( PlagueArmy.class, new PlagueArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( DarkRitual.class, new DarkRitual.MyJsonAdapter() );
    builder.registerTypeAdapter( DispelCombatMagic.class, new DispelCombatMagic.MyJsonAdapter() );
    builder.registerTypeAdapter( DeepFog.class, new DeepFog.MyJsonAdapter() );
    builder.registerTypeAdapter( ShadowAssassin.class, new ShadowAssassin.MyJsonAdapter() );
    builder.registerTypeAdapter( FireStorm.class, new FireStorm.MyJsonAdapter() );
    builder.registerTypeAdapter( DamageRegion.class, new DamageRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( ImbueRegion.class, new ImbueRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( CurseRegion.class, new CurseRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( DiplomaticAura.class, new DiplomaticAura.MyJsonAdapter() );
    builder.registerTypeAdapter( DemandSurrender.class, new DemandSurrender.MyJsonAdapter() );
    builder.registerTypeAdapter( Fireball.class, new Fireball.MyJsonAdapter() );
    builder.registerTypeAdapter( PhantomTroops.class, new PhantomTroops.MyJsonAdapter() );
    builder.registerTypeAdapter( MoveCapitol.class, new MoveCapitol.MyJsonAdapter() );
    builder.registerTypeAdapter( MagicDome.class, new MagicDome.MyJsonAdapter() );
    builder.registerTypeAdapter( SubvertHamlet.class, new SubvertHamlet.MyJsonAdapter() );
    builder.registerTypeAdapter( SubvertTown.class, new SubvertTown.MyJsonAdapter() );
    builder.registerTypeAdapter( SubvertCity.class, new SubvertCity.MyJsonAdapter() );
    builder.registerTypeAdapter( BadOmen.class, new BadOmen.MyJsonAdapter() );
    builder.registerTypeAdapter( CorruptEmbassy.class, new CorruptEmbassy.MyJsonAdapter() );
    builder.registerTypeAdapter( EnhanceEmbassy.class, new EnhanceEmbassy.MyJsonAdapter() );
    builder.registerTypeAdapter( EnhancePower.class, new EnhancePower.MyJsonAdapter() );
    builder.registerTypeAdapter( UnlimitedInvisibleArmy.class, new UnlimitedInvisibleArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( EnhancedInvisibleArmy.class, new EnhancedInvisibleArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( InvisibleArmy.class, new InvisibleArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( UnlimitedTeleportArmy.class, new UnlimitedTeleportArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( EnhancedTeleportArmy.class, new EnhancedTeleportArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( TeleportArmy.class, new TeleportArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( ShieldRegion.class, new ShieldRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( ShieldFigure.class, new ShieldFigure.MyJsonAdapter() );
    builder.registerTypeAdapter( CharmFigure.class, new CharmFigure.MyJsonAdapter() );
    builder.registerTypeAdapter( TeleportFigure.class, new TeleportFigure.MyJsonAdapter() );
    builder.registerTypeAdapter( CharmRegion.class, new CharmRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( DamagePopCenter.class, new DamagePopCenter.MyJsonAdapter() );
    builder.registerTypeAdapter( RaiseDead.class, new RaiseDead.MyJsonAdapter() );
    builder.registerTypeAdapter( SummonWerebeasts.class, new SummonWerebeasts.MyJsonAdapter() );
    builder.registerTypeAdapter( SummonOgres.class, new SummonOgres.MyJsonAdapter() );
    builder.registerTypeAdapter( SummonDragons.class, new SummonDragons.MyJsonAdapter() );
    builder.registerTypeAdapter( ImbuePopCenter.class, new ImbuePopCenter.MyJsonAdapter() );
    builder.registerTypeAdapter( TeleportSelf.class, new TeleportSelf.MyJsonAdapter() );
    builder.registerTypeAdapter( Alchemy.class, new Alchemy.MyJsonAdapter() );
    builder.registerTypeAdapter( Scry.class, new Scry.MyJsonAdapter() );
    builder.registerTypeAdapter( SabotagePopCenter.class, new SabotagePopCenter.MyJsonAdapter() );
    builder.registerTypeAdapter( SabotageEmbassy.class, new SabotageEmbassy.MyJsonAdapter() );
    builder.registerTypeAdapter( TakeRegionCensus.class, new TakeRegionCensus.MyJsonAdapter() );
    builder.registerTypeAdapter( MapRegion.class, new MapRegion.MyJsonAdapter() );
    builder.registerTypeAdapter( AgentScoutControlLevel.class, new AgentScoutControlLevel.MyJsonAdapter() );
    builder.registerTypeAdapter( AgentScoutEmbassies.class, new AgentScoutEmbassies.MyJsonAdapter() );
    builder.registerTypeAdapter( CounterEspionage.class, new CounterEspionage.MyJsonAdapter() );
    builder.registerTypeAdapter( AssassinateAgent.class, new AssassinateAgent.MyJsonAdapter() );
    builder.registerTypeAdapter( AssassinateDiplomat.class, new AssassinateDiplomat.MyJsonAdapter() );
    builder.registerTypeAdapter( AssassinateWizard.class, new AssassinateWizard.MyJsonAdapter() );
    builder.registerTypeAdapter( ImprovePower.class, new ImprovePower.MyJsonAdapter() );
    builder.registerTypeAdapter( ImproveEmbassy.class, new ImproveEmbassy.MyJsonAdapter() );
    builder.registerTypeAdapter( ImprovePopCenter.class, new ImprovePopCenter.MyJsonAdapter() );
    builder.registerTypeAdapter( CreateArmy.class, new CreateArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyCombine.class, new ArmyCombine.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyTransfer.class, new ArmyTransfer.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyRecruit.class, new ArmyRecruit.MyJsonAdapter() );
    builder.registerTypeAdapter( RetireUnit.class, new RetireUnit.MyJsonAdapter() );
    builder.registerTypeAdapter( TrainAgent.class, new TrainAgent.MyJsonAdapter() );
    builder.registerTypeAdapter( TrainWizard.class, new TrainWizard.MyJsonAdapter() );
    builder.registerTypeAdapter( TrainDiplomat.class, new TrainDiplomat.MyJsonAdapter() );
    builder.registerTypeAdapter( HireWizard.class, new HireWizard.MyJsonAdapter() );
    builder.registerTypeAdapter( HireDiplomat.class, new HireDiplomat.MyJsonAdapter() );
    builder.registerTypeAdapter( HireAgent.class, new HireAgent.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyAttackArmy.class, new ArmyAttackArmy.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyAttackPop.class, new ArmyAttackPop.MyJsonAdapter() );
    builder.registerTypeAdapter( AgentReconLocation.class, new AgentReconLocation.MyJsonAdapter() );
    builder.registerTypeAdapter( MoveWizard.class, new MoveWizard.MyJsonAdapter() );
    builder.registerTypeAdapter( MoveDiplomat.class, new MoveDiplomat.MyJsonAdapter() );
    builder.registerTypeAdapter( MoveAgent.class, new MoveAgent.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyMove.class, new ArmyMove.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmySearch.class, new ArmySearch.MyJsonAdapter() );
    builder.registerTypeAdapter( DiplomatInciteRebellion.class, new DiplomatInciteRebellion.MyJsonAdapter() );
    builder.registerTypeAdapter( DiplomatNegotiateFealty.class, new DiplomatNegotiateFealty.MyJsonAdapter() );
    builder.registerTypeAdapter( DiplomatInspireLoyalty.class, new DiplomatInspireLoyalty.MyJsonAdapter() );
    builder.registerTypeAdapter( Order.class, new Order.MyJsonAdapter<Order>() );
    builder.registerTypeAdapter( GameItem.class, new GameItem.MyJsonAdapter() );
    builder.registerTypeAdapter( Figure.class, new GameItem.MyJsonAdapter() );
    builder.registerTypeAdapter( AtomicInteger.class, new AtomicIntegerJsonAdapter() );
    builder.registerTypeAdapter( GameMap.class, new GameMap.MyJsonAdapter() );
    builder.registerTypeAdapter( Region.class, new Region.MyJsonAdapter() );
    builder.registerTypeAdapter( PopCenter.class, new PopCenter.MyJsonAdapter() );
    builder.registerTypeAdapter( Wizard.class, new Wizard.MyJsonAdapter() );
    builder.registerTypeAdapter( Diplomat.class, new Diplomat.MyJsonAdapter() );
    builder.registerTypeAdapter( Agent.class, new Agent.MyJsonAdapter() );
    builder.registerTypeAdapter( King.class, new King.MyJsonAdapter() );
    builder.registerTypeAdapter( Army.class, new Army.MyJsonAdapter() );
    builder.registerTypeAdapter( ArmyUnit.class, new ArmyUnit.MyJsonAdapter() );
    builder.registerTypeAdapter( Player.class, new Player.MyJsonAdapter() );
    return builder.create();
  }

  // ----------For testing only below this line--------------
  Game( String name, GameMap map )
  {
    _name = name;
    _map = map;
    _players = new ArrayList<>();
  }

  void setPlayers( List<Player> players )
  {
    _players.clear();
    _players.addAll( players );
  }
}