package kessel.hex.domain;

import kessel.hex.map.Region;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Defines all the kingdoms available in the game. */
@SuppressWarnings({ "unchecked" })
public class Kingdoms
{
  private static final Logger LOG = Logger.getLogger( Kingdoms.class );

  public static final Map<String, Kingdom> KINGDOMS = new TreeMap<>();

  static
  {
    try
    {
      KINGDOMS.putAll( loadKingdoms() );
    }
    catch ( IOException e )
    {
      LOG.error( "Failed to load kingdoms", e );
      throw new RuntimeException( "Failed to load kingdoms", e );
    }
  }

  private static Map<String, Kingdom> loadKingdoms() throws IOException
  {
    Map<String, Kingdom> kingdoms = new HashMap<>();
    Path kingdomDir = FileSystems.getDefault().getPath( "conf/kingdoms" );
    try
      (DirectoryStream<Path> dirStream = Files.newDirectoryStream( kingdomDir ))
    {
      for ( Path kingdomPath : dirStream )
      {
        Kingdom kingdom = loadKingdom( kingdomPath );
        kingdoms.put( kingdom.getName(), kingdom );
      }
    }
    return kingdoms;
  }

  /** @return the kingdom from the file definition, null if there isn't one. */
  private static Kingdom loadKingdom( Path kingdomPath ) throws IOException
  {
    try
      (Reader in = Files.newBufferedReader( kingdomPath, StandardCharsets.UTF_8 ))
    {
      // Read in the kingdom itself.
      String json = IOUtils.toString( in );
      Kingdom kingdom = Game.GSON.fromJson( json, Kingdom.class );
      LOG.debug( "Loaded kingdom: " + kingdom.getName() );

      // Read in the figure names.
      Path namesPath = FileSystems.getDefault().getPath( "conf/names/" + kingdom.getNamesFile() );
      List<String> names = FileUtils.readLines( namesPath.toFile() );
      kingdom.setFigureNames( names );

      return kingdom;
    }
  }

  public static void doStandardSetup( Player player, Game game )
  {
    int popCount = player.getPopCenters().size();
    for ( int i = 0; i < player.getArmies().size(); i++ )
    {
      Army army = player.getArmies().get( i );
      if ( army.isActive() )
      {
        PopCenter base = player.getPopCenters().get( i % popCount );
        army.setLocation( base.getLocation() );
      }
      else
      {
        army.setLocation( player.getCapitol().getLocation() );
      }
    }
    // Distribute kingdom figures/armies/etc in the player's initial pops.
    player.getKing().setBase( player.getCapitol() );
    for ( int i = 0; i < player.getAgents().size(); i++ )
    {
      Agent agent = player.getAgents().get( i );
      PopCenter base = player.getPopCenters().get( i % popCount );
      agent.setBase( base );
    }
    for ( int i = 0; i < player.getDiplomats().size(); i++ )
    {
      Diplomat diplomat = player.getDiplomats().get( i );
      PopCenter base = player.getPopCenters().get( i % popCount );
      diplomat.setBase( base );
    }
    // Wizards are distributed to both the military and pops, starting with the military.
    List<GameItem> wizardBases = new ArrayList<GameItem>( player.getActiveArmies() );
    wizardBases.addAll( player.getPopCenters() );
    for ( int i = 0; i < player.getWizards().size(); i++ )
    {
      Wizard wizard = player.getWizards().get( i );
      wizard.setBase( wizardBases.get( i % wizardBases.size() ) );
    }
  }

  public static void setupEmbassies( Player player, Game game )
  {
    // Assign an embassy bump to the regions the player has pops in, then randomly to any region after that.
    player.setupEmbassies( game.getMap().getRegions() );
    int embassiesBuilt = 0;
    for ( PopCenter popCenter : player.getPopCenters() )
    {
      if ( embassiesBuilt < player.getKingdom().getStartingEmbassies() )
      {
        player.improveEmbassy( popCenter.getLocation().getRegion() );
        embassiesBuilt++;
      }
    }
    Random _r = new Random();
    while ( embassiesBuilt < player.getKingdom().getStartingEmbassies() )
    {
      Region region = game.getMap().getRegions().get( _r.nextInt( game.getMap().getRegions().size() ) );
      player.improveEmbassy( region );
      embassiesBuilt++;
    }
  }

  /**
   * A programmatic way to spit various kingdoms out as JSON. The JSON is the "gold" version in a real game. This is useful as a way to
   * spit them to disk when iterating in the code. Each kingdom gets a file based on it's name.
   */
  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  public static void main( String[] args ) throws Exception
  {
    for ( Kingdom kingdom : KINGDOMS.values() )
    {
      String asJson = Game.GSON.toJson( kingdom, Kingdom.class );
      System.out.println( asJson );
    }
  }
}
