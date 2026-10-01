package de.cokechat;
import de.cokechat.config.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class RemovedFilterTest {
    @Test void legacyFiltersAreIgnoredAndDiscardedWithoutLosingAppearance(@TempDir Path temp) throws Exception {
        for(String mode:new String[]{"AUTO","DUNGEON","KUUDRA"}) {
            Path file=temp.resolve("config.json");
            Files.writeString(file,"{\"skyBlock\":{\"automaticDetection\":true,\"filterMode\":\""+mode+"\"},\"appearance\":{\"width\":420,\"chatBorder\":false}}");
            var manager=new ConfigManager(file);manager.load();
            assertEquals(420,manager.get().appearance.width);assertFalse(manager.get().appearance.chatBorder);
            manager.save();assertFalse(Files.readString(file).contains("skyBlock"));
        }
    }
}
