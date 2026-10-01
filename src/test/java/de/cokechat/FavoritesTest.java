package de.cokechat;
import de.cokechat.config.*;
import de.cokechat.emoji.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class FavoritesTest {
    @TempDir Path directory;
    @Test void favoritesPersistDeduplicateAndRemove() {
        var m=new ConfigManager(directory.resolve("config.json"));m.load();
        m.get().emoji.favorites.addAll(List.of("sob","skull","sob"));m.save();m.load();
        assertEquals(List.of("sob","skull"),m.get().emoji.favorites);
        m.get().emoji.favorites.remove("sob");m.save();m.load();assertEquals(List.of("skull"),m.get().emoji.favorites);
    }
    @Test void favoritesFirstWithoutLosingOrdinaryResults() {
        var db=EmojiDatabase.load(directory.resolve("emojis.json"));
        assertEquals("sunglasses",db.suggest("s",List.of("sunglasses")).getFirst());
        var matches=db.suggest("s",List.of("sob","skull"));assertTrue(matches.indexOf("sob")<2);assertTrue(matches.indexOf("skull")<2);
        assertTrue(db.suggest("smile",List.of("sob")).contains("smile"));
        assertFalse(db.search("smile").isEmpty());
    }
}
