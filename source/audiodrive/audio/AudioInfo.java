package audiodrive.audio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

public class AudioInfo {
	public String title;
	public String artist;
	public String album;
	
	public String duration;
	
	public AudioInfo(String path) throws Exception {
		this(new File(new URL(path).getFile()));
	}
	
	public AudioInfo(File file) {
		// Try read id3 Tags
		try {
			MP3File mp3file = new MP3File(file);
			final ID3v1 id3v1Tag = mp3file.getID3v1Tag();
			final AbstractID3v2 id3v2Tag = mp3file.getID3v2Tag();
			if (id3v2Tag != null) {
				title = id3v2Tag.getSongTitle();
				artist = id3v2Tag.getAuthorComposer();
				album = id3v2Tag.getAlbumTitle();
			}
			
			if (id3v1Tag != null) {
				title = title.isEmpty() ? id3v1Tag.getTitle() : title;
				artist = artist.isEmpty() ? id3v1Tag.getArtist() : artist;
				album = album.isEmpty() ? id3v1Tag.getAlbum() : album;
			}
			
			if (title.isEmpty()) title = file.getName();
		} catch (IOException | TagException e) {}
		// Try get duration
		try {
			
			AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
			Map properties = baseFileFormat.properties();
			Long duration = (Long) properties.get("duration");
			this.duration = String.format(
				"%d min, %d sec",
				TimeUnit.MICROSECONDS.toMinutes(duration),
				TimeUnit.MICROSECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(duration)));
		} catch (UnsupportedAudioFileException | IOException e) {}
	}
	
	public List<String> getInfos() {
		List<String> infos = new ArrayList<>();
		
		if (artist != null && !artist.isEmpty()) infos.add("Artist: " + artist);
		if (title != null && !title.isEmpty()) infos.add("Title: " + title);
		if (album != null && !album.isEmpty()) infos.add("Album: " + album);
		if (duration != null && !duration.isEmpty()) infos.add("Duration: " + duration);
		
		return infos;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (artist != null && !artist.isEmpty()) builder.append(artist + ", ");
		if (title != null && !title.isEmpty()) builder.append(title + ", ");
		if (album != null && !album.isEmpty()) builder.append(album + ", ");
		if (duration != null && !duration.isEmpty()) builder.append("Duration: " + duration);
		return builder.toString();
	}
}
