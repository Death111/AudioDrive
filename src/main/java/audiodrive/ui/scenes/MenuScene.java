package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.AudioDrive.Action;
import audiodrive.Resources;
import audiodrive.audio.AudioInfo;
import audiodrive.audio.AudioResource;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;
import audiodrive.ui.dialog.Dialog;
import audiodrive.ui.dialog.Dialog.DialogAnswer;
import audiodrive.ui.dialog.Dialog.DialogType;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.ui.menu.item.SettingsItem;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class MenuScene extends Scene implements ItemListener {
	
	private Text title;
	private Text version;
	private Text credits;
	private Menu menu;
	private SettingsItem<Integer> modelValue;
	private Menu modelSelectionMenu;
	private MenuItem visualizeMenuItem;
	private MenuItem playMenuItem;
	private MenuItem settingsMenuItem;
	private MenuItem exitMenuItem;
	private Overlay background;
	
	private AudioResource hoverAudio;
	private AudioResource selectAudio;
	
	private boolean silentHovering = true;
	private double volume;
	
	private Rotation rotation = new Rotation();
	private Text audioText;
	private List<Text> audioInformationText = new ArrayList<>();
	private List<String> models;
	
	private Map<String, Dialog> dialogs = new HashMap<>();
	private String dialog;
	
	@Override
	public void entering() {
		volume = AudioDrive.Settings.getDouble("interface.volume");
		AudioDrive.MenuSound.setVolume(AudioDrive.Settings.getDouble("sound.volume"));
		if (!AudioDrive.MenuSound.isRunning()) AudioDrive.MenuSound.setLooping(true).start();
		Log.trace("Entering MenueScene");
		
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setPosition(100, 80);
		audioText = new Text("Selected Audio").setFont(AudioDrive.Font).setSize(32).setPosition(600, 200);
		version = new Text("Version " + AudioDrive.Version).setFont(AudioDrive.Font).setSize(10).setPosition(10, getHeight() - 10).setAlignment(Alignment.LowerLeft);
		credits = new Text("Made by " + AudioDrive.Creators).setFont(AudioDrive.Font).setSize(10).setPosition(getWidth() - 10, getHeight() - 10).setAlignment(Alignment.LowerRight);
		
		menu = new Menu(100, 200, 400, 600, 25);
		playMenuItem = new MenuItem("Play", this);
		menu.addItem(playMenuItem);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addItem(visualizeMenuItem);
		settingsMenuItem = new MenuItem("Settings", this);
		menu.addItem(settingsMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addItem(exitMenuItem);
		
		hoverAudio = new AudioResource("sounds/Hover.wav");
		selectAudio = new AudioResource("sounds/Select.wav");
		
		background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Title.fs"));
		
		models = Resources.getAvailablePlayerModelPaths();
		AudioDrive.setPlayerModel(Resources.getCurrentPlayerModel());
		modelSelectionMenu = new Menu(600, 400, 600 + 1, 51, 0);
		List<Integer> modelIndexe = new ArrayList<>();
		int currentModel = 1;
		for (int i = 0; i < models.size(); i++) {
			modelIndexe.add(i + 1);
			if (models.get(i).contains(AudioDrive.Settings.get("player.model"))) currentModel = i + 1;
		}
		modelValue = new SettingsItem<Integer>("Selected Model", modelIndexe, 600, 50, this);
		modelValue.setValue(currentModel);
		modelSelectionMenu.addItem(modelValue);
		Input.addObservers(menu, modelSelectionMenu);
		
		dialogs.put("exit", new Dialog("Exit AudioDrive?", DialogType.YES_NO));
		if (dialog != null) openDialog(dialog);
		
		audioInformationText.clear();
		// Test if audio was selected
		if (AudioDrive.getSelectedAudio() == null) {
			audioInformationText.add(new Text("Non selected").setFont(AudioDrive.Font).setSize(24).setPosition(600, 250));
			return;
		}
		// Try to parse tags
		try {
			final AudioInfo audioInfo = new AudioInfo(AudioDrive.getSelectedAudio().getPath());
			int offset = 25; // Spacing between text
			final List<String> infos = audioInfo.getInfos();
			IntStream.range(0, infos.size()).forEach(idx -> {
				audioInformationText.add(new Text(infos.get(idx)).setFont(AudioDrive.Font).setSize(24).setPosition(600, 250 + idx * offset));
			});
			
		} catch (Exception e) {
			// Something went wrong i.e. no tags available use file name
			Log.warning("Error while parsing tags: " + e);
			audioInformationText.clear();
			audioInformationText.add(new Text(AudioDrive.getSelectedAudio().getName()).setFont(AudioDrive.Font).setSize(24).setPosition(600, 250));
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Camera.overlay(getWidth(), getHeight());
		background.render();
		title.render();
		
		version.render();
		credits.render();
		menu.render();
		
		audioText.render();
		audioInformationText.stream().forEach(Text::render);
		modelSelectionMenu.render();
		
		Camera.perspective(45, getWidth(), getHeight(), .1, 100);
		Camera.position(new Vector(.3, .2, -1));
		Camera.lookAt(new Vector(0, 0, 1));
		rotation.apply();
		double factor = getHeight() / 1080.0;
		Model playerModel = AudioDrive.getPlayerModel();
		playerModel.position().y(-0.15 * (1.1 - factor));
		playerModel.scale(0.05 * factor).render();
		
		if (dialog != null) {
			Camera.overlay(getWidth(), getHeight());
			dialogs.get(dialog).render();
		}
		
		checkDialogAnswered();
	}
	
	@Override
	public void update(double time) {
		rotation.yAdd(10 * time);
	}
	
	private void openDialog(String name) {
		if (name == null || dialogs.get(name) == null) return;
		dialog = name;
		dialogs.get(dialog).activate();
		Input.removeObservers(menu, modelSelectionMenu);
	}
	
	private void checkDialogAnswered() {
		if (dialog == null) return;
		Dialog currentDialog = dialogs.get(dialog);
		if (currentDialog == null || currentDialog.answer == null) return;
		if (dialog.equals("exit")) {
			if (currentDialog.answer == DialogAnswer.YES) {
				AudioDrive.exit();
			} else {
				exitMenuItem.reset();
			}
		}
		Input.addObservers(menu, modelSelectionMenu);
		currentDialog.reset();
		dialog = null;
	}
	
	@Override
	public void exiting() {
		if (!Window.isRecreating() && getEntering() == null) AudioDrive.MenuSound.stop();
		if (AudioDrive.getAction() == Action.Play || AudioDrive.getAction() == Action.Visualize) AudioDrive.MenuSound.setLooping(false);
		Input.removeObservers(menu, modelSelectionMenu);
		dialogs.clear();
		title = null;
		version = null;
		credits = null;
		background = null;
		menu = null;
		modelSelectionMenu = null;
		visualizeMenuItem = null;
		playMenuItem = null;
		settingsMenuItem = null;
		exitMenuItem = null;
		hoverAudio = null;
		selectAudio = null;
		System.gc();
	}
	
	@Override
	public void keyReleased(int key, char character) {
		if (dialog != null) {
			switch (key) {
			case Keyboard.KEY_ESCAPE:
				dialogs.get(dialog).cancel();
				break;
			case Keyboard.KEY_RETURN:
				dialogs.get(dialog).confirm();
				break;
			}
			return;
		}
		
		super.keyReleased(key, character);
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			onSelect(exitMenuItem, true);
			break;
		case Keyboard.KEY_V:
			onSelect(visualizeMenuItem, true);
			break;
		case Keyboard.KEY_RETURN:
		case Keyboard.KEY_P:
			onSelect(playMenuItem, true);
			break;
		case Keyboard.KEY_M:
			Scene.get(ModelScene.class).enter();
			break;
		case Keyboard.KEY_S:
			onSelect(settingsMenuItem, true);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onHover(Item item, boolean hover) {
		if (hover && !silentHovering) {
			hoverAudio.play(volume);
		}
	}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) return;
		selectAudio.play(volume);
		AudioDrive.setAction(Action.None);
		if (item == visualizeMenuItem) {
			AudioDrive.setAction(Action.Visualize);
			Scene.get(SelectionScene.class).enter();
			return;
		}
		if (item == playMenuItem) {
			AudioDrive.setAction(Action.Play);
			Scene.get(SelectionScene.class).enter();
			return;
		}
		if (item == settingsMenuItem) {
			Scene.get(SettingsScene.class).enter();
			return;
		}
		if (item == modelValue) {
			final String modelPath = models.get(modelValue.getValue() - 1);
			Model playerModel = ModelLoader.loadModel(modelPath);
			AudioDrive.setPlayerModel(playerModel);
			AudioDrive.Settings.set("player.model", playerModel.getName());
			return;
		}
		if (item == exitMenuItem) {
			openDialog("exit");
			return;
		}
	}
	
}
