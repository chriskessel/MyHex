package kessel.hex.map;

import kessel.hex.domain.Game;
import kessel.hex.domain.Kingdoms;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static kessel.hex.domain.PopCenter.PopType.*;

/** Responsible for map creation, which basically means the algorithms for creating the map. */
public class MapCreator
{
  private static final Logger LOG = Logger.getLogger( MapCreator.class );
  static final Terrain DEFAULT_TERRAIN = Terrain.Plain;

  private final Map<Terrain, List<String>> _regionFlavorNames = new EnumMap<>( Terrain.class );
  private final Map<Terrain, AtomicInteger> _regionFlavorNamesIndex = new EnumMap<>( Terrain.class );

  private final Game _game;
  GameMap _map;
  private final Random _r = new Random();
  private final Map<Tuple, Region> _regionForCenter = new HashMap<>();
  private int _regionSize;
  private int _maxRegionSize;
  private int _minRegionSize;
  private int _numRegions;
  private int _numPlayers;
  private int _townsPerRegion;
  private int _mapWidth;
  private int _mapHeight;

  public MapCreator( Game game, MapCreationListener listener )
  {
    _game = game;
    if ( listener != null ) { _listener = listener; }
  }

  /**
   * This creates a square map that gets as close a possible to the size neccessary, without going over, to support the combination of
   * players/regions/etc.
   */
  public GameMap createMap( int numPlayers, int hexesPerPlayer, double regionsPerPlayer, int townsPerPlayer )
  {
    _numPlayers = numPlayers;
    _numRegions = (int) ((double) numPlayers * regionsPerPlayer);
    _townsPerRegion = townsPerPlayer * numPlayers / _numRegions;

    _map = new GameMap( numPlayers, hexesPerPlayer );
    _mapWidth = _map.getWidth();
    _mapHeight = _map.getHeight();
    _game.setMap( _map );
    initializeMap();
    allocatePlayerPops();
    setupPlayers();
    spreadTerrain(); // Terrain spread is based on player positions, so must come after setupPlayers()
    setupEmbassies(); // Has to happen after spreadTerrain() because regions are flavored by the regional city's terrain
    return _map;
  }

  public void initializeMap()
  {
    _regionSize = (_mapWidth * _mapHeight) / _numRegions;
    _maxRegionSize = (int) ((double) _regionSize * 1.30);
    _minRegionSize = (int) ((double) _regionSize * 0.70);
    initRegionNames();
    createRegions( _map );
    createPopCenters();
  }

  /** Create the regions. This just allocated hexes to regions, nothing else. */
  private void createRegions( GameMap map )
  {
    map.initLocations();
    map.clearRegions();
    _regionForCenter.clear();

    // We use two helper collections that deserve mention:
    // - unallocatedHexes: the algorithm may leave a few odd spots unassigned that have to be specially allocated at the end.
    // - regionEdgeHexes: there's no point in trying to expand from non-edge hexes.
    List<Tuple> unallocatedHexes = createAllCoordList();
    Map<Tuple, List<Tuple>> regionEdgeHexes = new HashMap<>();

    // Create region centers as dispersed as possible and allocate each region its surrounding area.
    int regionDistanceRadius = determineDispersedHexRadius( _regionSize );
    int coreRegionRadius = regionDistanceRadius - 1;
    determineRegionCenters( regionDistanceRadius, coreRegionRadius, _numRegions );
    map.getRegions().addAll( _regionForCenter.values() );
    allocateRegionCores( coreRegionRadius );
    for ( Map.Entry<Tuple, Region> regionEntry : _regionForCenter.entrySet() )
    {
      Tuple regionCenter = regionEntry.getKey();
      Region region = regionEntry.getValue();
      regionEdgeHexes.put( regionCenter, HexCalculator.getEdgeNeighbors( regionCenter, coreRegionRadius ) );
      unallocatedHexes.removeAll( getTuplesForLocations( region.getLocations() ) );
    }
    _listener.mapChanged();

    // Allocate hexes until they're all gone.
    while ( !unallocatedHexes.isEmpty() )
    {
      // Try to allocate one hex to each region.
      List<Tuple> absorbedHexes = new ArrayList<>();
      for ( Map.Entry<Tuple, Region> regionEntry : _regionForCenter.entrySet() )
      {
        Tuple absorbedHex = allocateHexToRegion( regionEntry.getValue(), regionEdgeHexes.get( regionEntry.getKey() ) );
        if ( absorbedHex != null )
        {
          absorbedHexes.add( absorbedHex );
        }
      }

      // It's possible no one can allocate due to allocation rules. Let fixUnallocatedHexes() handle it.
      unallocatedHexes.removeAll( absorbedHexes );
      if ( absorbedHexes.isEmpty() )
      {
        break;
      }
    }

    fixUnallocatedHexes( unallocatedHexes );
    _listener.mapChanged();
    if ( !regionsAreValid() )
    {
      createRegions( map );
    }
  }


  /** Goals: - All regions get a proportional number of towns - 1 city per region, multiple towns, multiple hamlets */
  private void createPopCenters()
  {
    List<String> names = loadPopNames();
    for ( Map.Entry<Tuple, Region> regionEntry : _regionForCenter.entrySet() )
    {
      Tuple regionCenter = regionEntry.getKey();
      Region region = regionEntry.getValue();

      // Promote the town closest to the region center to a city that's an interior hex.
      List<Tuple> popCenterHexes = findPopCenterHexesForRegion( region );
      List<Tuple> potentialCityLocations = new ArrayList<>( popCenterHexes );
      Tuple cityHex = null;
      while ( !potentialCityLocations.isEmpty() )
      {
        cityHex = HexCalculator.getClosest( regionCenter, potentialCityLocations, _mapWidth / 2 );
        if ( isInteriorHex( cityHex ) )
        {
          break; // Found a pop hex that's interior to the region, we're good.
        }
        else
        {
          potentialCityLocations.remove( cityHex );
        }
      }

      // Didn't find an interior pop center, just use the closest one to the region center.
      if ( cityHex == null ) { cityHex = HexCalculator.getClosest( regionCenter, popCenterHexes, _mapWidth / 2 ); }

      popCenterHexes.remove( cityHex );
      Location cityLocation = _map.getLocation( cityHex );
      PopCenter city = new PopCenter( _game.generateUniqueId(), names.remove( 0 ), 0, cityLocation, City );
      city.setLevel( Game.BASE_CITY_PRODUCTION + (_r.nextInt( 3 ) - 1) );
      region.addPopCenter( city );
      _game.addPopCenter( city );

      // The 1/2 (rounded up) of the remaining become hamlets, the rest become towns.
      for ( int i = 0; i < popCenterHexes.size(); i++ )
      {
        Tuple popCenterHex = popCenterHexes.get( i );
        Location popCenterLocation = _map.getLocation( popCenterHex );
        int randomLevelTweak = _r.nextInt( 3 ) - 1;
        PopCenter popCenter;
        if ( i % 2 == 0 )
        {
          popCenter = new PopCenter( _game.generateUniqueId(), names.remove( 0 ), 0, popCenterLocation, Hamlet );
          popCenter.setLevel( Game.BASE_HAMLET_PRODUCTION + randomLevelTweak );
        }
        else
        {
          popCenter = new PopCenter( _game.generateUniqueId(), names.remove( 0 ), 0, popCenterLocation, Town );
          popCenter.setLevel( Game.BASE_TOWN_PRODUCTION + randomLevelTweak );
        }
        region.addPopCenter( popCenter );
        _game.addPopCenter( popCenter );
      }
    }
    _listener.mapChanged();
  }


  // Find equidistant starting hexes for each player.
  public void allocatePlayerPops()
  {
    int hexesPerPlayer = (_mapWidth * _mapHeight) / _numPlayers;
    int playerDistance = determineDispersedHexRadius( hexesPerPlayer );
    List<Tuple> allTowns = getTuplesForPopCenters( _game.getPopCenters( Town ) );
    List<Tuple> playerCenters = findDispersedHexes( new ArrayList<Tuple>(), playerDistance, _numPlayers, 0, allTowns );
    List<Tuple> unassignedTowns = new ArrayList<>( allTowns );
    List<Tuple> allHamlets = getTuplesForPopCenters( _game.getPopCenters( Hamlet ) );
    List<Tuple> unassignedHamlets = new ArrayList<>( allHamlets );
    List<Player> players = _game.getPlayers();
    for ( int playerIndex = 0; playerIndex < _numPlayers; playerIndex++ )
    {
      // Allocate the player's capitol. It gets a 50% bump in level.
      Player player = players.get( playerIndex );
      Tuple playerCenter = playerCenters.get( playerIndex );
      Tuple capitolHex = HexCalculator.getClosest( playerCenter, unassignedTowns, _mapWidth / 2 );
      unassignedTowns.remove( capitolHex );
      PopCenter capitolTown = _game.getPopCenter( capitolHex );
      capitolTown.setLevel( (int) (Game.BASE_TOWN_PRODUCTION * 1.34) );
      player.setCapitol( capitolTown );
      player.add( capitolTown );

      // Allocate the player's other town as close as possible, but in another region. Player pops are all 1
      // stronger than normal.
      Region playerRegion = _map.getLocation( playerCenter ).getRegion();
      List<Tuple> hexesInRegion = getTuplesForLocations( playerRegion.getLocations() );
      for ( int searchRadius = 1; ; searchRadius++ )
      {
        List<Tuple> neighbors = HexCalculator.getEdgeNeighbors( capitolHex, searchRadius );
        neighbors.retainAll( unassignedTowns );
        neighbors.removeAll( hexesInRegion );
        if ( !neighbors.isEmpty() )
        {
          Tuple townHex = neighbors.get( _r.nextInt( neighbors.size() ) );
          PopCenter playerTown = _game.getPopCenter( townHex );
          playerTown.setLevel( Game.BASE_TOWN_PRODUCTION + 1 );
          player.add( playerTown );
          unassignedTowns.remove( townHex );
          break;
        }
      }

      // Allocate the player's hamlet in the same region. Player pops are all 1 stronger than normal.
      hexesInRegion.retainAll( unassignedHamlets );
      Tuple hamletHex = hexesInRegion.get( _r.nextInt( hexesInRegion.size() ) );
      unassignedHamlets.remove( hamletHex );
      PopCenter playerHamlet = _game.getPopCenter( hamletHex );
      playerHamlet.setLevel( Game.BASE_HAMLET_PRODUCTION + 1 );
      player.add( playerHamlet );
    }
    _listener.mapChanged();
  }


  /** For each player, spread favorable terrain around their starting pop centers. */
  public void spreadTerrain()
  {
    // Figure out how much of the board to cover with terrain.
    // - Leave a portion the default.
    // - The rest is allocated based on player pops, with capitols getting twice the spread of non-capitols.
    // - Also randomly spread from other areas so terrain doesn't give a clue about where players started.
    int coverable = (int) (_mapWidth * _mapHeight * 0.80);
    int playerSpreads = _game.getPlayers().size();
    int numTerrains = Terrain.values().length - 1;
    int randomSpreads = (int) (Math.ceil( _map.getRegions().size() / 2.0 ) * numTerrains);
    int spreadAmount = coverable / (playerSpreads + randomSpreads);

    // Spread from each player's capitol.
    List<TerrainSpreadGoal> spreadGoals = new ArrayList<>();
    for ( Player player : _game.getPlayers() )
    {
      for ( PopCenter popCenter : player.getPopCenters() )
      {
        // Randomly adjust how many hexes get allocated to make things a little more organic looking.
        int adjustment = _r.nextInt( 3 ) - 1;
        if ( popCenter.equals( player.getCapitol() ) )
        {
          spreadGoals.add( new TerrainSpreadGoal(
            popCenter.getLocation(), player.getKingdom().getTerrainPreferences().get( 0 ), 2 * (spreadAmount + adjustment) ) );
        }
      }
    }

    // Spread from a bunch of random places to fill out the map.
    List<Tuple> nonPlayerTuples = createAllCoordList();
    nonPlayerTuples.removeAll( getTuplesForPopCenters( _game.getAllPlayerCapitols() ) );
    List<Tuple> nonPlayerSpreadCenters = findDispersedHexes( new ArrayList<Tuple>(), 1, randomSpreads, 1, nonPlayerTuples );
    int spreadTypeIndex = 0;
    for ( Tuple hex : nonPlayerSpreadCenters )
    {
      int adjustment = _r.nextInt( 3 ) - 1;
      Terrain terrain = Terrain.values()[spreadTypeIndex++ % numTerrains];
      spreadGoals.add( new TerrainSpreadGoal( _map.getLocation( hex ), terrain, spreadAmount + adjustment ) );
    }
    doSpreadTerrain( spreadGoals );

    // Any terrain still untouched gets the default value.
    for ( Region region : _regionForCenter.values() )
    {
      for ( Location location : region.getLocations() )
      {
        if ( location.getTerrain() == Terrain.Undefined )
        {
          location.setTerrain( DEFAULT_TERRAIN );

        }
      }
    }

    // Now, go through the regional cities and give the region fun terrain-based names.
    for ( PopCenter city : _game.getPopCenters( PopCenter.PopType.City ) )
    {
      String regionName = nextRegionName( city.getLocation().getTerrain() );
      city.getLocation().getRegion().setName( regionName );
    }
    _listener.mapChanged();
  }

  /** Spread one hex for each TerrainSpreadGoal, repeating until all spreading is complete. */
  private void doSpreadTerrain( List<TerrainSpreadGoal> spreadList )
  {
    // Initialize all spreads with their starting point.
    for ( TerrainSpreadGoal spread : spreadList )
    {
      spread._startingPoint.setTerrain( spread._terrain );
      spread._leftToSpread--;
      spread._spreadableFrom.add( spread._startingPoint );
    }

    // Cycle through each spread point and spread one hex at a time until spreading is complete.
    while ( !spreadList.isEmpty() )
    {
      for ( TerrainSpreadGoal spread : new ArrayList<>( spreadList ) )
      {
        spreadTerrainOneHex( spread );

        // We're done with this spread point if allocation is done or we're locked out of places to spread to.
        if ( (spread._leftToSpread == 0) || spread._spreadableFrom.isEmpty() )
        {
          spreadList.remove( spread );
        }
      }
    }
  }

  private void spreadTerrainOneHex( TerrainSpreadGoal spread )
  {
    boolean spreadSuccess = false;
    while ( !spreadSuccess && !spread._spreadableFrom.isEmpty() )
    {
      Location spreadFrom = spread._spreadableFrom.get( _r.nextInt( spread._spreadableFrom.size() ) );
      List<Tuple> spreadTargets = HexCalculator.getEdgeNeighbors( spreadFrom.getCoord(), 1 );
      GameMap.removeOffMapTuples( spreadTargets, _mapWidth, _mapHeight );
      for ( Iterator<Tuple> it = spreadTargets.iterator(); it.hasNext(); )
      {
        Location spreadTarget = _map.getLocation( it.next() );

        // Spread if the target has no terrain and doesn't contain a player owned Pop.
        boolean hasNoTerrain = spreadTarget.getTerrain() == Terrain.Undefined;
        PopCenter popAtLocation = _game.getPopCenter( spreadTarget.getCoord() );
        boolean notPlayerCapitol = (popAtLocation == null) || (!popAtLocation.isCapitol());
        if ( hasNoTerrain && notPlayerCapitol )
        {
          spreadTarget.setTerrain( spread._terrain );
          spread._spreadableFrom.add( spreadTarget );
          spread._leftToSpread--;
          spreadSuccess = true;
          _listener.mapChanged();
//                    try
//                    {
//                        Thread.sleep( 50 );
//                    }
//                    catch ( InterruptedException e )
//                    {
//                        e.printStackTrace();
//                    }
          break;
        }
        else
        {
          it.remove();
        }
      }

      // If this hex couldn't spread, remove it as a candidate for future spreads.
      if ( !spreadSuccess )
      {
        spread._spreadableFrom.remove( spreadFrom );
      }
    }
  }

  /** Find the smallest radius that contains the desired number of hexes. */
  private int determineDispersedHexRadius( int hexesPerArea )
  {
    int containedSoFar = 1;
    int currentRadius = 1;
    while ( containedSoFar < hexesPerArea )
    {
      containedSoFar += currentRadius * 6;
      currentRadius++;
    }
    return currentRadius - 2;
  }

  private void determineRegionCenters( int distanceRadius, int coreRadius, int numRegions )
  {
    // Set each region center location as owned by a region. Now, the core radius must be < distance radius.
    List<Tuple> regionCenters = findDispersedHexes(
      new ArrayList<Tuple>(), distanceRadius, numRegions, coreRadius, createAllCoordList() );
    for ( int i = 0; i < regionCenters.size(); i++ )
    {
      Tuple regionCenter = regionCenters.get( i );
      Region region = new Region( "Region " + i );
      _regionForCenter.put( regionCenter, region );
      _map.getLocation( regionCenter ).setRegion( region );
    }
  }

  /**
   * Allocate the points around a starting point to the region owned by that starting point. This prevents narrow regions and prevents
   * regions going last on the hex allocation from being squeezed out.
   *
   * @param absorbRadius how much around the centers to allocate
   */
  private void allocateRegionCores( int absorbRadius )
  {
    for ( Map.Entry<Tuple, Region> regionEntry : _regionForCenter.entrySet() )
    {
      Tuple regionCenter = regionEntry.getKey();
      Region region = regionEntry.getValue();
      for ( Tuple hexToAbsorb : HexCalculator.getAllNeighbors( regionCenter, absorbRadius ) )
      {
        Location locationToAbsorb = _map.getLocation( hexToAbsorb );
        if ( locationToAbsorb.getRegion().equals( Region.UNKNOWN_REGION ) )
        {
          region.addLocation( locationToAbsorb );
          locationToAbsorb.setRegion( region );
        }
      }
    }
  }

  /** @return the allocated hex or null if an allocation wasn't possible. */
  private Tuple allocateHexToRegion( Region region, List<Tuple> edgeHexes )
  {
    List<Tuple> allHexes = getTuplesForLocations( region.getLocations() );
    boolean regionIsMaxSize = allHexes.size() == _maxRegionSize;
    if ( regionIsMaxSize ) { return null; }

    // Keep picking edge hexes to expand from until we find a valid target or run out of edge hexes.
    List<Tuple> workingEdgeHexes = new ArrayList<>( edgeHexes );
    while ( !workingEdgeHexes.isEmpty() )
    {
      Tuple expandHex = workingEdgeHexes.get( _r.nextInt( workingEdgeHexes.size() ) );
      if ( !isLockedIn( expandHex ) )
      {
        Tuple allocatedHex = findAbsorbableHex( expandHex );
        if ( allocatedHex != null )
        {
          edgeHexes.add( allocatedHex );
          allHexes.add( allocatedHex );
          Location location = _map.getLocation( allocatedHex );
          location.setRegion( region );
          region.addLocation( location );
          _listener.mapChanged();
          return allocatedHex;
        }
        else
        {
          // Legal hex, but can't expand from it right now due to some map building constraint.
          workingEdgeHexes.remove( expandHex );
        }
      }
      else
      {
        // Hex is surrounded by allocated hexes, stop trying to expand from it.
        workingEdgeHexes.remove( expandHex );
        edgeHexes.remove( expandHex );
      }
    }
    return null;
  }

  // This shouldn't happen, but there's a hole in the algorithm for corners sometimes. Just allocated to an adjacent region.
  private void fixUnallocatedHexes( List<Tuple> unallocatedHexes )
  {
    while ( !unallocatedHexes.isEmpty() )
    {
      for ( Iterator<Tuple> it = unallocatedHexes.iterator(); it.hasNext(); )
      {
        Tuple unallocatedHex = it.next();
        List<Tuple> neighbors = HexCalculator.getEdgeNeighbors( unallocatedHex, 1 );
        GameMap.removeOffMapTuples( neighbors, _mapWidth, _mapHeight );
        for ( Tuple neighbor : neighbors )
        {
          Region neighborRegion = _map.getLocation( neighbor ).getRegion();
          if ( !neighborRegion.equals( Region.UNKNOWN_REGION ) )
          {
            Location location = _map.getLocation( unallocatedHex );
            location.setRegion( neighborRegion );
            neighborRegion.addLocation( location );
            it.remove();
            _listener.mapChanged();
            break;
          }
        }
      }
    }
  }

  // If any region is +/- 25% of desired size, then this was a failure.
  private boolean regionsAreValid()
  {
    for ( Region region : _regionForCenter.values() )
    {
      int regionSize = region.getLocations().size();
      if ( (regionSize < _minRegionSize) || (regionSize > _maxRegionSize) )
      {
        return false;
      }
    }
    return true;
  }

  /**
   * The algorithm: 1 - Randomly choose a hex as a starting point, then keep randomly picking hexes that don't overlap with it. 2a -
   * repeat step 1 until all areas are found, or 2b - if we can't find enough valid areas, the hex radius requirement is decremented and
   * we start again, but with the points we've already found as valid hexes.
   *
   * @param presetPoints points already predetermined
   * @param hexRadius    a guidelines for how far around a hex must be clear of other starting points.
   * @param numAreas     how many areas find, if possible.
   * @param edgeDistance how close to the edge a spot can be. 0 means right on the edge is allowed
   * @param legalHexes   which hexes can actually be used as chosen points.
   * @return The starting points on the map. It may be less than the desired number, but never more.
   */
  private List<Tuple> findDispersedHexes(
    List<Tuple> presetPoints, int hexRadius, int numAreas, int edgeDistance, List<Tuple> legalHexes )
  {
    // First, allocate as many as we can spread farther apart than necessary. This gives us a better spread overall.
    int fudgedHexRadius = hexRadius + 1;

    // All the tuples available for allocation, so we know what's available around a potential starting tuple.
    List<Tuple> allTuplesAvailable = createAllCoordList();

    // Track valid starting tuples separately, so we can rule out bad ones as we go along.
    List<Tuple> centerTuplesAvailable = new ArrayList<>( legalHexes );
    removeMapEdges( centerTuplesAvailable, edgeDistance );

    // Remove tuples related to the predetermined starting points at the previously higher hex radius.
    for ( Tuple presetPoint : presetPoints )
    {
      List<Tuple> neighbors = HexCalculator.getAllNeighbors( presetPoint, fudgedHexRadius + 1 );
      centerTuplesAvailable.removeAll( neighbors );
      allTuplesAvailable.removeAll( neighbors );
    }

    // Find the new tuples.
    List<Tuple> centerTuples = new ArrayList<>( presetPoints );
    while ( (centerTuples.size() < numAreas) &&
            !centerTuplesAvailable.isEmpty() )
    {
      // Pick a candidate starting point at random.
      Tuple candidate = centerTuplesAvailable.remove( _r.nextInt( centerTuplesAvailable.size() ) );

      // Find all the hexes within the radius.
      List<Tuple> neighbors = HexCalculator.getAllNeighbors( candidate, fudgedHexRadius );
      GameMap.removeOffMapTuples( neighbors, _mapWidth, _mapHeight );

      // If all the neighbors all still available, it's a good candidate.
      if ( allTuplesAvailable.containsAll( neighbors ) )
      {
        centerTuples.add( candidate );

        // Remove all the neighbors and anyone within range of those neighbors as candidates for future center points.
        List<Tuple> toExclude = HexCalculator.getAllNeighbors( candidate, fudgedHexRadius * 2 );
        allTuplesAvailable.removeAll( toExclude );
        centerTuplesAvailable.removeAll( toExclude );
      }
    }

    // If we didn't find enough starting points, find the rest with the radius dropped by one.
    if ( centerTuples.size() < numAreas )
    {
      centerTuples = findDispersedHexes( centerTuples, hexRadius - 1, numAreas, edgeDistance, legalHexes );
    }
    return centerTuples;
  }

  /** @return true if the hex is surrounded by hexes already allocated to a region. */
  private boolean isLockedIn( Tuple expandHex )
  {
    List<Tuple> neighbors = HexCalculator.getEdgeNeighbors( expandHex, 1 );
    GameMap.removeOffMapTuples( neighbors, _mapWidth, _mapHeight );
    for ( Tuple neighbor : neighbors )
    {
      if ( _map.getLocation( neighbor ).getRegion().equals( Region.UNKNOWN_REGION ) )
      {
        return false;
      }
    }
    return true;
  }


  // Find an unowned hex next to the expansion target that's closest to the region center.
  private Tuple findAbsorbableHex( Tuple expandHex )
  {
    List<Tuple> absorbableTargets = HexCalculator.getEdgeNeighbors( expandHex, 1 );
    GameMap.removeOffMapTuples( absorbableTargets, _mapWidth, _mapHeight );
    removeAllocatedLocations( absorbableTargets );
    removePeninsulaLocations( expandHex, absorbableTargets );

    return absorbableTargets.isEmpty() ? null : absorbableTargets.get( 0 );
  }


  private void removeAllocatedLocations( List<Tuple> absorbableTargets )
  {
    for ( Iterator<Tuple> iter = absorbableTargets.iterator(); iter.hasNext(); )
    {
      Tuple target = iter.next();
      if ( !_map.getLocation( target ).getRegion().equals( Region.UNKNOWN_REGION ) )
      {
        iter.remove();
      }
    }
  }

  /**
   * Remove any locations that would create a peninsula. Effectively, this means that an absorbable target must be next to at least 2
   * other hexes of the same region.
   */
  @SuppressWarnings("ObjectEquality")
  private void removePeninsulaLocations( Tuple expandHex, List<Tuple> absorbableTargets )
  {
    Region myRegion = _map.getLocation( expandHex ).getRegion();
    for ( Iterator<Tuple> iter = absorbableTargets.iterator(); iter.hasNext(); )
    {
      Tuple target = iter.next();
      List<Tuple> targetNeighbors = HexCalculator.getEdgeNeighbors( target, 1 );
      GameMap.removeOffMapTuples( targetNeighbors, _mapWidth, _mapHeight );
      int neighborsInRegion = 0;
      for ( Tuple targetNeighbor : targetNeighbors )
      {
        if ( _map.getLocation( targetNeighbor ).getRegion() == myRegion )
        {
          neighborsInRegion++;
        }
      }

      if ( neighborsInRegion < 2 )
      {
        iter.remove();
      }
    }
  }

  /**
   * Remove all tuples from the input list if they're within the given distance of the map edge.
   *
   * @param allTuples      the tuples to check
   * @param distanceToEdge how close to the edge is too close
   */
  private void removeMapEdges( List<Tuple> allTuples, int distanceToEdge )
  {
    int maxX = (_mapWidth - 1) - distanceToEdge;
    int maxY = (_mapHeight - 1) - distanceToEdge;
    for ( Iterator<Tuple> iter = allTuples.iterator(); iter.hasNext(); )
    {
      Tuple tuple = iter.next();
      if ( (tuple.x < distanceToEdge) || (tuple.y < distanceToEdge) ||
           (tuple.x > maxX) || (tuple.y > maxY) )
      {
        iter.remove();
      }
    }
  }

  /**
   * Add pop centers to a Region. Keep the pop centers somewhat spread out by splitting the hexes into chunks and assigning one pop center
   * per chunk.
   *
   * @return the hexes that actually received pop centers
   */
  private List<Tuple> findPopCenterHexesForRegion( Region region )
  {
    double regionSizeAdjustment = (double) region.getLocations().size() / (double) _regionSize;
    int numTowns = (int) Math.round( regionSizeAdjustment * (double) _townsPerRegion );

    List<Tuple> regionHexes = getTuplesForLocations( region.getLocations() );
    int hexesPerTown = regionHexes.size() / numTowns;
    List<Tuple> popCenters = new ArrayList<>();
    for ( int i = 0; i < numTowns; i++ )
    {
      Tuple townHex = regionHexes.get( (i * hexesPerTown) + _r.nextInt( hexesPerTown ) );
      popCenters.add( townHex );
    }
    return popCenters;
  }

  /** @return true if the hex is surrounded by hexes of the same region. */
  private boolean isInteriorHex( Tuple hex )
  {
    Location hexLocation = _map.getLocation( hex );
    List<Tuple> neighbors = HexCalculator.getEdgeNeighbors( hex, 1 );
    GameMap.removeOffMapTuples( neighbors, _mapWidth, _mapHeight );
    for ( Tuple neighbor : neighbors )
    {
      if ( !_map.getLocation( neighbor ).getRegion().equals( hexLocation.getRegion() ) )
      {
        return false;
      }
    }
    return true;
  }

  private List<Tuple> createAllCoordList()
  {
    List<Tuple> list = new ArrayList<>();
    for ( int x = 0; x < _mapWidth; x++ )
    {
      for ( int y = 0; y < _mapHeight; y++ )
      {
        list.add( new Tuple( x, y ) );
      }
    }
    return list;
  }

  /** Allocates the player's starting kingdom resources to the game/map. */
  void setupPlayers()
  {
    for ( Player player : _game.getPlayers() )
    {
      Kingdoms.doStandardSetup( player, _game );
    }
  }

  private void setupEmbassies()
  {
    for ( Player player : _game.getPlayers() )
    {
      Kingdoms.setupEmbassies( player, _game );
    }
  }

  private static List<Tuple> getTuplesForLocations( List<Location> locations )
  {
    List<Tuple> tuples = new ArrayList<>();
    for ( Location location : locations )
    {
      tuples.add( location.getCoord() );
    }
    return tuples;
  }

  private static List<Tuple> getTuplesForPopCenters( List<PopCenter> popCenters )
  {
    List<Tuple> tuples = new ArrayList<>();
    for ( PopCenter popCenter : popCenters )
    {
      tuples.add( popCenter.getLocation().getCoord() );
    }
    return tuples;
  }

  /** Loads the possible population center names from disk, shuffling the returned collection so it's random each game. */
  @SuppressWarnings({ "unchecked" })
  private static List<String> loadPopNames()
  {
    List<String> names;
    Path namesPath = FileSystems.getDefault().getPath( "conf/names/CityNames.txt" );
    try
    {
      names = FileUtils.readLines( namesPath.toFile() );
    }
    catch ( IOException e )
    {
      throw new RuntimeException( "Couldn't find pop center names file.", e );
    }
    Collections.shuffle( names );
    return names;
  }

  public void initRegionNames()
  {
    for ( Terrain terrain : Terrain.values() )
    {
      switch ( terrain )
      {
        case Undefined:
          break;
        default:
          loadTerrainRegionNames( terrain );
          break;
      }
    }
  }

  @SuppressWarnings({ "unchecked" })
  private void loadTerrainRegionNames( Terrain terrain )
  {
    Path namesPath = FileSystems.getDefault().getPath( "conf/names/" + terrain.name() + "RegionNames.txt" );
    try
    {
      List<String> names = FileUtils.readLines( namesPath.toFile() );
      Collections.shuffle( names ); // shuffle so the names are always the same each game.
      _regionFlavorNames.put( terrain, names );
      _regionFlavorNamesIndex.put( terrain, new AtomicInteger( 0 ) );
    }
    catch ( IOException e )
    {
      throw new RuntimeException( "Failure reading terrain names file.", e );

    }
  }

  public String nextRegionName( Terrain terrain )
  {
    String name = _regionFlavorNames.get( terrain ).get( _regionFlavorNamesIndex.get( terrain ).getAndIncrement() );
    return name;
  }

  public interface MapCreationListener
  {
    void mapChanged();
  }

  // A default implementation that doesn't do anything.
  private transient MapCreationListener _listener = new MapCreationListener()
  {
    public void mapChanged()
    {
    }
  };

  public void setCreationListener( MapCreationListener listener ) { _listener = listener; }

  // Helper for terrain spread during map generation.
  static class TerrainSpreadGoal
  {
    Location _startingPoint;
    int _leftToSpread;
    List<Location> _spreadableFrom = new ArrayList<>();
    Terrain _terrain;

    TerrainSpreadGoal( Location startingPoint, Terrain terrain, int amount )
    {
      _terrain = terrain;
      _leftToSpread = amount;
      _startingPoint = startingPoint;
    }
  }
}
