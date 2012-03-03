package kessel.hex.admin;

import kessel.hex.domain.Figure;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.Order;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Gives the details on the selected hex in the map. */
@SuppressWarnings({ "RawUseOfParameterizedType" })
class PlayerDetailsPane extends AbstractDetailsPane
{
  // Details on the selected hex.
  JPanel _hexDetailsPane;

  // The player's current orders.
  JList _ordersList;

  // The kingdom's items.
  JPanel _kingdomDetailsPane;

  // Finance labels.
  JLabel _ordersCostLabel;
  JLabel _treasuryLabel;
  JLabel _productionLabel;
  JLabel _armyCostLabel;
  JLabel _troopsAvailableLabel;
  JLabel _powerLabel;
  JLabel _armySupportLabel;

  PlayerDetailsPane( PlayerDisplay gameDisplay )
  {
    super( gameDisplay );
  }

  protected void layoutComponents()
  {
    setLayout( new GridLayout( 3, 1 ) );

    // The pane for details of the hex selected.
    _hexDetailsPane = new JPanel();
    _hexDetailsPane.setLayout( new BoxLayout( _hexDetailsPane, BoxLayout.Y_AXIS ) );
    _hexDetailsPane.setBorder( BorderFactory.createEtchedBorder() );
    JScrollPane hexDetailsScrollPane = new JScrollPane( _hexDetailsPane );

    // The orders list.
    JPanel ordersPanel = new JPanel( new BorderLayout() );
    JLabel ordersLabel = new JLabel( "Current Orders" );
    _ordersList = new JList();
    _ordersList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    _ordersList.setVisibleRowCount( 10 );
    _ordersList.addListSelectionListener( createOrderSelectionListener() );
    JScrollPane listScroller = new JScrollPane( _ordersList );
    ordersPanel.add( ordersLabel, BorderLayout.NORTH );
    ordersPanel.add( listScroller, BorderLayout.CENTER );

    // Kingdom details.
    // .. items
    JPanel kingdomPanel = new JPanel( new BorderLayout() );
    _kingdomDetailsPane = new JPanel();
    _kingdomDetailsPane.setLayout( new BoxLayout( _kingdomDetailsPane, BoxLayout.Y_AXIS ) );
    _kingdomDetailsPane.setBorder( BorderFactory.createEtchedBorder() );
    JScrollPane kingdomDetailsScrollPane = new JScrollPane( _kingdomDetailsPane );

    // .. finances
    JPanel moneyPanel = new JPanel( new GridLayout( 4, 2 ) );
    _ordersCostLabel = new JLabel();
    _armyCostLabel = new JLabel();
    _treasuryLabel = new JLabel();
    _productionLabel = new JLabel();
    _troopsAvailableLabel = new JLabel();
    _powerLabel = new JLabel();
    _armySupportLabel = new JLabel();
    moneyPanel.add( _ordersCostLabel );
    moneyPanel.add( _armyCostLabel );
    moneyPanel.add( _treasuryLabel );
    moneyPanel.add( _productionLabel );
    moneyPanel.add( _troopsAvailableLabel );
    moneyPanel.add( _powerLabel );
    moneyPanel.add( _armySupportLabel );
    kingdomPanel.add( kingdomDetailsScrollPane, BorderLayout.CENTER );
    kingdomPanel.add( moneyPanel, BorderLayout.SOUTH );

    add( hexDetailsScrollPane );
    add( ordersPanel );
    add( kingdomPanel );
  }

  private ListSelectionListener createOrderSelectionListener()
  {
    return new ListSelectionListener()
    {
      public void valueChanged( ListSelectionEvent e )
      {
        if ( !e.getValueIsAdjusting() )
        {
          int selection = ((JList) e.getSource()).getSelectedIndex();
          if ( selection >= 0 )
          {
            Order order = getPlayer().getNextTurnOrders().get( selection );
            _gameDisplay.setSelectedHex( order.getOrderLocation().getCoord() );
            _gameDisplay.repaint();
          }
        }
      }
    };
  }

  protected JPanel getHexDetailsPanel() { return _hexDetailsPane; }

  public void updateDetails()
  {
    super.updateDetails();

    if ( getPlayer() != null )
    {
      // Update the kingdom details.
      List<GameItem> playerItems = new ArrayList<>( getPlayer().getAllItems() );
      Collections.sort( playerItems, PlayerItemComparator.INSTANCE );
      _kingdomDetailsPane.removeAll();
      for ( GameItem item : playerItems )
      {
        JLabel itemLabel = new PlayerItemLabel( item );
        itemLabel.addMouseListener( _playerItemListener );
        _kingdomDetailsPane.add( itemLabel );
      }
      validate();
      repaint();
    }
  }

  /** A label for a player item. */
  private static class PlayerItemLabel extends JLabel
  {
    final GameItem _item;

    PlayerItemLabel( GameItem item )
    {
      _item = item;
      setText( item.getDescription() );
    }
  }

  /** Listens for clicks on the player items and warps the selected hex to the item's location */
  MouseListener _playerItemListener = new MouseAdapter()
  {
    public void mouseClicked( MouseEvent e )
    {
      if ( e.getSource() instanceof PlayerItemLabel )
      {
        _gameDisplay.setSelectedHex( ((PlayerItemLabel) e.getSource())._item.getLocation().getCoord() );
        _gameDisplay.repaint();
        validate();
        repaint();
      }
    }
  };

  public void init()
  {
    super.init();
    updateOrders();
  }

  /**
   * Force the orders list to update
   *
   * @param newOrder a new order to add, which is set as the selected order.
   */
  void updateOrders( Order newOrder )
  {
    _ordersList.removeAll();
    List<String> orderDescriptions = new ArrayList<>();
    String orderToSelect = null;
    Player player = getPlayer();
    if ( player != null )
    {
      int i = 0;
      for ( Order order : player.getNextTurnOrders() )
      {
        orderDescriptions.add( (i + 1) + "(" + order.getOrderCost() + "): " + order.getShortDescription() );
        if ( (newOrder != null) && newOrder.equals( order ) )
        {
          orderToSelect = orderDescriptions.get( i );
        }
        i++;
      }
    }
    _ordersList.setListData( orderDescriptions.toArray() );
    if ( orderToSelect != null )
    {
      _ordersList.setSelectedValue( orderToSelect, true );
    }
    _ordersList.validate();
    _ordersList.repaint();

    if ( player != null )
    {
      updateEstimatedCostLabel();
      _armyCostLabel.setText( "Army Cost: " + player.getTroopCost() );
      _treasuryLabel.setText( "Treasury: " + player.getGold() );
      _productionLabel.setText( "Production: " + player.getCurrentProduction() );
      _troopsAvailableLabel.setText( "Kingdom Recruits: " + player.getKingdomTroopsAvailable() );
      _powerLabel.setText( "Power: " + player.getPower() );
      _armySupportLabel.setText( "Army Support: " + player.getSupportRequired() + "(" + player.getSupportCapacity() + ")" );
    }
  }

  void updateOrders() { updateOrders( null ); }

  private void updateEstimatedCostLabel()
  {
    int cost = 0;
    for ( Order order : getPlayer().getNextTurnOrders() )
    {
      cost += order.getOrderCost();
    }
    _ordersCostLabel.setText( "Order Cost: " + cost );
  }

  /** Add the item's status messages from the previous turn's activities. */
  protected void addItemStatusMessages( GameItem item, JPanel detailsContainer )
  {
    super.addItemStatusMessages( item, detailsContainer );

    if ( isOrderable( item ) )
    {
      List<Order<GameItem>> orders = new ArrayList<>( item.getNextTurnOrders() );
      for ( Order<GameItem> order : orders )
      {
        // Add a label describing order and give it a cancel popup.
        JLabel orderLabel = createInfoLabel( order.getShortDescription(), Font.ITALIC );
        JPopupMenu cancelMenu = new JPopupMenu();
        JMenuItem cancelMenuItem = new JMenuItem( createCancelAction( order, item ) );
        cancelMenu.add( cancelMenuItem );
        orderLabel.addMouseListener( new OrderMenuListener( cancelMenu ) );
        detailsContainer.add( orderLabel );
      }

      // Support giving the item a new order (if it can accept one).
      boolean hasOrdersLeft = getPlayer().getNextTurnOrders().size() < getPlayer().getPower();
      List<Class> availableOrders = item.getAvailableOrders();
      if ( hasOrdersLeft && !availableOrders.isEmpty() )
      {
        JPopupMenu newOrderMenu = new JPopupMenu();
        for ( Class orderType : availableOrders )
        {
          JMenuItem menuItem = new JMenuItem( createOrderAction( orderType, item ) );
          newOrderMenu.add( menuItem );
        }
        JLabel newOrderLabel = createInfoLabel( Order.NEW_ORDER.getShortDescription(), Font.ITALIC );
        newOrderLabel.addMouseListener( new OrderMenuListener( newOrderMenu ) );
        detailsContainer.add( newOrderLabel );
      }
    }
  }

  private boolean isOrderable( GameItem item )
  {
    // A player can only order their own items.
    return item.getOwner().equals( getPlayer() );
  }

  Player getPlayer() { return ((PlayerDisplay) _gameDisplay)._player; }

  public void setActionInProgress( OrderAction action )
  {
    _gameDisplay.setActionInProgress( action );
  }

  public Order getSelectedOrder()
  {
    int i = _ordersList.getSelectedIndex();
    return i >= 0 ? getPlayer().getNextTurnOrders().get( i ) : null;
  }

  public Map<String, Color> getPlayerColors()
  {
    return _gameDisplay.getPlayerColors();
  }

  /** Listens for mouse event on order components. */
  private static class OrderMenuListener extends MouseAdapter
  {
    private final JPopupMenu _orderMenu;

    private OrderMenuListener( JPopupMenu orderMenu )
    {
      _orderMenu = orderMenu;
    }

    public void mousePressed( MouseEvent e )
    {
      _orderMenu.show( e.getComponent(), e.getX(), e.getY() );
    }
  }

  // Create an action that creates the given order.
  Action createOrderAction( Class orderType, GameItem item )
  {
    Action action = ImpliedTargetActions.createAction( this, orderType, item );
    if ( action == null )
    {
      action = SingleTargetActions.createAction( this, orderType, item );
    }
    if ( action == null )
    {
      action = ArmyTransferActions.createAction( this, orderType, item );
    }
    if ( action == null )
    {
      action = ArmyMoveActions.createAction( this, orderType, item );
    }
    if ( action == null )
    {
      throw new RuntimeException( "Unknown order type: " + orderType.getName() );
    }
    else
    {
      return action;
    }
  }

  // Create an action that cancels the given order.
  public Action createCancelAction( Order<GameItem> order, GameItem item )
  {
    return new ImpliedTargetActions.CancelAction( this, order, item );
  }

  // Order all the items for one player in the following order.
  // - kings
  // - diplomats
  // - agents
  // - wizards
  // - armies
  // - pop centers
  protected static class PlayerItemComparator implements Comparator<GameItem>
  {
    enum DisplayOrder
    {
      King, Diplomat, Agent, Wizard, Army, PopCenter
    }

    protected static final Comparator<GameItem> INSTANCE = new PlayerItemComparator();

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
        else if ( itemA instanceof PopCenter )
        {
          i = ((Integer) ((PopCenter) itemB).getLevel()).compareTo( ((PopCenter) itemA).getLevel() );
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
