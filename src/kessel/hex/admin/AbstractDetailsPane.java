package kessel.hex.admin;

import kessel.hex.domain.Army;
import kessel.hex.domain.Figure;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.PopCenter;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Gives the details on the selected hex in the map. */
abstract class AbstractDetailsPane extends JPanel
{
  protected final AbstractGameDisplay _gameDisplay;

  AbstractDetailsPane( AbstractGameDisplay gameDisplay )
  {
    super();
    _gameDisplay = gameDisplay;
    layoutComponents();
  }

  /** Subclasses can override this to add other components and change the layout. */
  protected void layoutComponents()
  {
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
  }

  public Dimension getPreferredSize()
  {
    return new Dimension( 400, _gameDisplay.getMapHeight() );
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks" })
  public void updateDetails()
  {
    JPanel detailsPanel = getHexDetailsPanel();
    List<GameItem> items = _gameDisplay.getItemsAtHex( _gameDisplay._selectedHex );

    // Group the items by "base" type, then by owner.
    // - First, the town itself
    //   - Figures/items in the town
    // - Armies
    //   - figures in the army
    SortedMap<GameItem, SortedSet<GameItem>> groupedItems = new TreeMap<>( ItemComparator.INSTANCE );
    for ( GameItem item : items )
    {
      if ( item instanceof PopCenter || item instanceof Army )
      {
        if ( !groupedItems.containsKey( item ) )
        {
          groupedItems.put( item, new TreeSet<>( ItemComparator.INSTANCE ) );
        }
      }
      else if ( item instanceof Figure )
      {
        Figure figure = (Figure) item;
        GameItem base = figure.getBase();
        SortedSet<GameItem> itemsAtBase = groupedItems.get( base );
        if ( itemsAtBase == null )
        {
          itemsAtBase = new TreeSet<>( ItemComparator.INSTANCE );
          groupedItems.put( base, itemsAtBase );
        }
        itemsAtBase.add( figure );
      }
    }

    // Now that we've got everything ordered, fill in the pane with the game item display panels.
    detailsPanel.removeAll();
    for ( Map.Entry<GameItem, SortedSet<GameItem>> entry : groupedItems.entrySet() )
    {
      JPanel groupItemPanel = createGroupItem( entry );
      detailsPanel.add( groupItemPanel );
    }
    validate();
    repaint();
  }

  protected JPanel getHexDetailsPanel() { return this; }

  /** @return A panel showing all the items in a given base item (town or army). */
  protected JPanel createGroupItem( Map.Entry<GameItem, SortedSet<GameItem>> entry )
  {
    GameItem groupItem = entry.getKey();
    SortedSet<GameItem> groupedItems = entry.getValue();
    JPanel groupItemPanel = createEtchedBoxLayoutPanel( BoxLayout.Y_AXIS );

    // Add a row for the group item itself.
    JPanel baseItemPanel = createItemRow( groupItem, false );
    groupItemPanel.add( baseItemPanel );

    // Add rows for the items inside the group item.
    for ( GameItem item : groupedItems )
    {
      JPanel rowContainer = createItemRow( item, true );
      groupItemPanel.add( rowContainer );
    }
    return groupItemPanel;
  }

  /** @return a panel holding all the bits for an item: name, status, orders given, etc. */
  protected JPanel createItemRow( GameItem item, boolean indent )
  {
    JPanel owningContainer = createPlainBoxLayoutPanel( BoxLayout.X_AXIS );
    addIndent( indent, owningContainer );
    addItemDetails( item, owningContainer );
    return owningContainer;
  }

  protected void addIndent( boolean indent, JPanel owningContainer )
  {
    if ( indent )
    {
      JLabel indentLabel = new JLabel( "  " );
      owningContainer.add( indentLabel );
    }
  }

  /** Add all the item details: description, status, current orders, etc. */
  protected void addItemDetails( GameItem item, JPanel owningContainer )
  {
    JPanel detailsContainer = createPlainBoxLayoutPanel( BoxLayout.Y_AXIS );
    addItemDescription( item, detailsContainer );
    addItemStatusMessages( item, detailsContainer );
    owningContainer.add( detailsContainer );
  }

  protected void addItemDescription( GameItem item, JPanel detailsContainer )
  {
    JLabel itemLabel = new JLabel( item.getLongStatusName(), SwingConstants.LEFT );
    int rowHeight = (int) itemLabel.getMaximumSize().getHeight(); // make width extend to end of container
    itemLabel.setMaximumSize( new Dimension( Integer.MAX_VALUE, rowHeight ) );
    detailsContainer.add( itemLabel );
  }

  /** Add a status message about what the item did last turn. */
  protected void addItemStatusMessages( GameItem item, JPanel detailsContainer )
  {
    for ( String s : item.getLastTurnStatus() )
    {
      JLabel lastOrderLabel = createInfoLabel( s, Font.PLAIN );
      detailsContainer.add( lastOrderLabel );
    }
  }

  protected JPanel createEtchedBoxLayoutPanel( int layoutAxis )
  {
    return createBoxLayoutPanel( layoutAxis, true );
  }

  protected JPanel createPlainBoxLayoutPanel( int layoutAxis )
  {
    return createBoxLayoutPanel( layoutAxis, false );
  }

  protected JPanel createBoxLayoutPanel( int layoutAxis, boolean etchBorder )
  {
    JPanel boxPanel = new JPanel();
    boxPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
    boxPanel.setLayout( new BoxLayout( boxPanel, layoutAxis ) );
    if ( etchBorder ) { boxPanel.setBorder( BorderFactory.createEtchedBorder() ); }
    return boxPanel;
  }

  protected JLabel createInfoLabel( String text, int fontStyle )
  {
    JLabel orderLabel = new JLabel( text, SwingConstants.LEFT );
    orderLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
    Font font = orderLabel.getFont().deriveFont( fontStyle, (float) orderLabel.getFont().getSize() - 2 );
    orderLabel.setFont( font );
    int rowHeight = (int) orderLabel.getMaximumSize().getHeight(); // make width extend to end of container
    orderLabel.setMaximumSize( new Dimension( Integer.MAX_VALUE, rowHeight ) );
    return orderLabel;
  }

  public void init()
  {
    updateDetails();
  }

  // Ensures all items are in a consistent order.
  public static class ItemComparator implements Comparator<GameItem>
  {
    enum DisplayOrder
    {
      PopCenter, King, Diplomat, Agent, Wizard, Army
    }

    protected static final Comparator<GameItem> INSTANCE = new ItemComparator();

    @SuppressWarnings({ "ChainOfInstanceofChecks" })
    public int compare( GameItem itemA, GameItem itemB )
    {
      int i = DisplayOrder.valueOf( itemA.getClass().getSimpleName() ).compareTo(
        DisplayOrder.valueOf( itemB.getClass().getSimpleName() ) );
      if ( i == 0 )
      {
        if ( itemA instanceof Figure )
        {
          i = ((Integer) ((Figure) itemB).getLevel()).compareTo( ((Figure) itemA).getLevel() );
        }
      }
      if ( i == 0 )
      {
        i = itemA.getName().compareTo( itemB.getName() );
      }
      return i;
    }
  }
}
