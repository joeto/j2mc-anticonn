package to.joe.j2mc.anticonn;

import java.util.HashMap;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class Monitor extends JavaPlugin implements Listener {
    private static final Pattern ipattern = Pattern.compile("(?<=(^|[(\\p{Space}|\\p{Punct})]))((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])(?=([(\\p{Space}|\\p{Punct})]|$))");
    private HashMap<String, Integer> map=new HashMap<String,Integer>();
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void prelogin(PlayerPreLoginEvent event){
        if(event.getResult()==Result.KICK_BANNED){
            String ip=event.getAddress().getHostAddress();
            check(ip);
        }
    }
    
    private void check(String ip){
        if(map.containsKey(ip)){
            int count=map.get(ip);
            count++;
            if(count>=5){
                this.getServer().banIP(ip);
                this.getLogger().info("Banning "+ip+" for spamjoin");
                return;
            }
            map.put(ip, count);
        }
    }
    
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getLogger().setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                final String message = record.getMessage();
                if (message.contains("Failed to verify username!") && !message.contains("<")) {
                    final Matcher matcher = ipattern.matcher(message);
                    if (matcher.find()) {
                        final String ip = matcher.group();
                        check(ip);
                    }
                }
                return true;
            }

        });
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run(){
                map.clear();
            }
        }, 1200, 1200);
    }
}
