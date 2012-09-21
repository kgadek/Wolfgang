package wolfgang.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import wolfgang.data.DataMaster;
import wolfgang.data.mapper.Category;
import wolfgang.data.mapper.Operation;
import wolfgang.data.mapper.User;
import wolfgang.utils.Utils;
import wolfgang.utils.Utils.Function2;

public class MainWindow {

	private DataMaster dm;
	private User logged;
	
	public MainWindow() throws ClassNotFoundException, NoSuchAlgorithmException, SecurityException, SQLException, IOException {
		super();
		dm = DataMaster.getInstance();
		System.out.println("Users:");
		for(User u : dm.getUsers())
			System.out.println("\t\t"+u);
		System.out.println("Categories:");
		for(Category c : dm.getCategories())
			System.out.println("\t\t"+c);
		System.out.println("Operations:");
		for(Operation o : dm.getOperations())
			System.out.println("\t\t"+o);
	}
	
	private static MainWindow mainWindow;
	private static JFrame loginFrame;
	private static JFrame mainFrame;
	private static JTextField loginTextField;
	private static JTextField passTextField;

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					mainWindow = new MainWindow();
					
					
					loginFrame = new JFrame("Logowanie");
					GridBagLayout gbl = new GridBagLayout();
					GridBagConstraints c = new GridBagConstraints();
					loginFrame.setLayout(gbl);
					
					c.fill = GridBagConstraints.HORIZONTAL;
					c.weightx = 100;
					c.weighty = 20;
					
					loginFrame.add(loginTextField = new JTextField("login"), c);
					loginFrame.add(passTextField = new JTextField("pass"), c);
					
					JButton logMeIn = new JButton();
					logMeIn.setText("Logowanie");
					logMeIn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if((mainWindow.logged = mainWindow.verifyUser(loginTextField.getText(), passTextField.getText())) != null) {
								loginFrame.setVisible(false);
								mainFrame = mainWindow.createMainWindow();
								mainFrame.setVisible(true);
							} else
								JOptionPane.showMessageDialog(loginFrame, "Błąd logowania");
						}
					});
					loginFrame.add(logMeIn, c);
					
					JButton createUser = new JButton();
					createUser.setText("Nowy");
					createUser.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if("".equals(loginTextField.getText()) || "".equals(passTextField.getText()))
								JOptionPane.showMessageDialog(loginFrame, "Nie podano loginu i/lub hasła");
							else {
								try {
									if((mainWindow.logged = mainWindow.createOrGetUser(loginTextField.getText(), passTextField.getText())) == null)
										JOptionPane.showMessageDialog(loginFrame, "Błąd: taki użytkownik już istnieje");
									else {
										loginFrame.setVisible(false);
										mainFrame = mainWindow.createMainWindow();
										mainFrame.setVisible(true);
									}
								} catch (SQLException e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(loginFrame, "Błąd:" + e1.getMessage());
								}
							}
						}
					});
					loginFrame.add(createUser);
					
					loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					loginFrame.pack();
					loginFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	protected User createOrGetUser(String user, String password) throws SQLException {
		return dm.createOrGetUser(loginTextField.getText(), new Date(), passTextField.getText());
	}
	
	protected User verifyUser(String user, String password) {
		return dm.checkLogin(user, password);
	}

	public JFrame createMainWindow() {
		JFrame mainFrame = new JFrame("Wolfgang");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane mainPane = new JTabbedPane();
		
		JComponent podsumowaniePane = genPodsumowaniePane();
		mainPane.addTab("Podsumowanie", podsumowaniePane);
		
		JComponent operacjePane = genOperacjePane();
		mainPane.addTab("Operacje", operacjePane);
		
		JComponent kategoriePane = genKategoriePane();
		mainPane.addTab("Kategorie", kategoriePane);
		
		mainPane.setSelectedIndex(1);
		mainFrame.add(mainPane);
		
//		mainFrame.pack();
		mainFrame.setSize(700, 500);
		return mainFrame;
	}

	private static JPanel genPodsumowaniePane() {
		return new JPanel(false);
	}

	private static JPanel genKategoriePane() {
		return new JPanel(false);
	}

	@SuppressWarnings("serial")
	public class OperationTable extends JPanel {
		private JTable table;
		private DefaultTableModel tm;
		
		public void reloadData() {
			String[] colNames = { "Kategoria", "Opis", "Data", "Bilans", "Saldo" };
			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
			Collection<Operation> oops = wolfgang.utils.Utils.filter(dm.getOperations(), new Utils.Predicate<Operation>() {
				@Override public boolean apply(Operation type) {
					return type.user.id == logged.id;
				}
			});
			Object[][] data = new Object[oops.size()][];
			List<Operation> res = new ArrayList<Operation>(oops);
			Collections.sort(res, new Comparator<Operation>() {
				@Override public int compare(Operation o1, Operation o2) {
					return (int) (o2.dateStart.getTime() - o1.dateStart.getTime());
				}
			});
			int i = 0;
			// this is map
			for(Operation o : res)
					data[i++] = new Object[] { o.category.description, o.description, formatter.format(o.dateStart), o.balance, o.finalBalance };
			
			tm.setDataVector(data, colNames);
		}
		
		public OperationTable() {
			super(new GridLayout(1, 1));
			
			tm = new DefaultTableModel();
			
			reloadData();
			
			table = new JTable(tm);
			table.setPreferredScrollableViewportSize(new Dimension(500, 300));
			table.setFillsViewportHeight(true);
			
			table.getColumnModel().getColumn(0).setPreferredWidth(100);
			
			table.getColumnModel().getColumn(1).setPreferredWidth(400);
			
			table.getColumnModel().getColumn(2).setPreferredWidth(100);
			DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
			centerAlign.setHorizontalAlignment(JLabel.CENTER);
			table.getColumnModel().getColumn(2).setCellRenderer(centerAlign);
			
			table.getColumnModel().getColumn(3).setPreferredWidth(80);
			DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
			rightAlign.setHorizontalAlignment(JLabel.RIGHT);
			table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
			
			table.getColumnModel().getColumn(4).setPreferredWidth(80);
			table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
			
			add(new JScrollPane(table));
		}
		
	}
	
	private JFrame genAddOperationPane() {
		final JFrame frame = new JFrame("Wolfgang - dodaj operację");
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		frame.setLayout(layout);
		
		JLabel lbl;
		InputVerifier verify_isfloat;
		JButton btn;
		
		verify_isfloat = new InputVerifier() {
			public boolean verify(JComponent comp) {
				JTextField textField = (JTextField) comp;
				try {
					Float.parseFloat(textField.getText());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		};
		
		lbl = new JLabel("opis:");
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(lbl,c);
		
		final JTextField dtf = new JTextField("");
		c.gridy = 0;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(dtf,c); 
		
		lbl = new JLabel("balans:");
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(lbl,c);
		
		final JTextField tf = new JTextField("");
		tf.setInputVerifier(verify_isfloat);
		c.gridy = 1;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(tf,c);
		
		lbl = new JLabel("kategoria:");
		c.gridy = 2;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(lbl,c);
		
		
		Collection<String> a = Utils.map2(new Function2<Category, String>() {
			@Override public String apply(Category x) {
				return String.valueOf( x.description );
			}
		}, dm.getCategories());
		
		final JComboBox<String> cmbo_str = new JComboBox<String>(a.toArray(new String[] {}));
		cmbo_str.setEditable(false);
		c.gridy = 2;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.HORIZONTAL;
		frame.add(cmbo_str,c);
		
		btn = new JButton("Dodaj kategorię");
		btn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String descr = (String) cmbo_str.getSelectedObjects()[0];				
				Category ctg = dm.getCategoryByName(descr);
				boolean ok = false;
				try {
					dm.createOperation(logged, ctg, 0, 0, new Integer(0), new Date(), 0, new Date(0), 0, descr);
					ok = true;
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(loginFrame, "Błąd");
				}
				if(ok)
					frame.dispose();
			}
		});
		c.gridy = 3;
		c.gridx = 1;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.LINE_END;
		frame.add(btn, c);
		
		//frame.setSize(350, 400);
		frame.setMinimumSize(new Dimension(350, 0));
		frame.pack();
		return frame;
	}	
	
	
	private JPanel genOperacjePane() {
		JPanel ret = new JPanel(false);
		GridBagLayout layout = new GridBagLayout();
		ret.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		JButton button;
		
		OperationTable table = new OperationTable();
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 200;
		c.weighty = 100;
		c.fill = GridBagConstraints.BOTH;
		ret.add(table,c);
		
		button = new JButton("Dodaj operację");
		c.weightx = 50;
		c.weighty = 50;
		c.gridy = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTH;
		c.anchor = GridBagConstraints.PAGE_START;
		button.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				genAddOperationPane().setVisible(true);
			}
		});
		ret.add(button,c);
		
		button = new JButton("lol10");
		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.NORTHWEST;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		ret.add(button,c);
		
		button = new JButton("lol11");
		c.gridy = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.NORTHEAST;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		ret.add(button,c);
		
		return ret;
	}

}
