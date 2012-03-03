package kessel.hex.orders.wizard;

import com.google.gson.reflect.TypeToken;
import kessel.hex.domain.Game;
import kessel.hex.domain.Kingdom;
import kessel.hex.domain.Kingdoms;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.Order;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Common implementation for all Wizard spells. */
abstract class AbstractSpell extends Order<Wizard>
{
  private static final Logger LOG = Logger.getLogger( Kingdoms.class );
  public static final Map<String, Integer> SPELLS_BY_LEVEL = new TreeMap<>();
  static
  {
    Path spellFile = FileSystems.getDefault().getPath( "conf/spell_levels.json" );
    try (Reader in = Files.newBufferedReader( spellFile, StandardCharsets.UTF_8 ))
    {
      Map<String,Integer> spellsByLevel = Game.GSON.fromJson( in, new TypeToken<Map<String,Integer>>(){}.getType() );
      SPELLS_BY_LEVEL.putAll( spellsByLevel );
    }
    catch ( IOException e )
    {
      LOG.error( "Failure loading spells list.", e );
      throw new RuntimeException( e );
    }
  }

  protected AbstractSpell() { super(); } // GSON only
  protected AbstractSpell( Wizard wizard )
  {
    super( wizard );
  }

  @SuppressWarnings({ "RedundantIfStatement" })
  protected boolean canExecute( Game game )
  {
    if ( !super.canExecute( game ) ) return false;
    if ( handleAlreadyActedThisTurn( game ) ) return false;
    if ( handleNotSufficientLevel( game ) ) return false;
    return true;
  }

  protected boolean handleNotSufficientLevel( Game game )
  {
    if ( _subject.getLevel() < SPELLS_BY_LEVEL.get( getClass().getSimpleName() ) )
    {
      addPlayerEvent( game, _subject, _subject.getName() + " is not high enough level to cast the spell." );
      return true;
    }
    return false;
  }
}
