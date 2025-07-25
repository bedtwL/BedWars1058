package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.LastHit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.getAPI;

public class FireballListener implements Listener {
    @EventHandler
    public void fireballHit(ProjectileHitEvent e) {
        if(!(e.getEntity() instanceof Fireball)) return;
        Location location = e.getEntity().getLocation();

        ProjectileSource projectileSource = e.getEntity().getShooter();
        if(!(projectileSource instanceof Player)) return;
        Player source = (Player) projectileSource;

        Vector vector = location.toVector();

        World world = location.getWorld();

        assert world != null;

        double fireballExplosionSize = 3;
        Collection<Entity> nearbyEntities = world
                .getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);
        for(Entity entity : nearbyEntities) {
            if(!(entity instanceof Player)) continue;
            Player player = (Player) entity;
            if(!getAPI().getArenaUtil().isPlaying(player)&&!player.hasPermission("bedtwl.bypass.fb.gamemode")) continue;


            Vector playerVector = player.getLocation().toVector();
            Vector normalizedVector = vector.subtract(playerVector).normalize();
            double fireballHorizontal = -1.0;
            Vector horizontalVector = normalizedVector.multiply(fireballHorizontal);
            double fireballVertical = 1.5;
            double y = 0.65 * fireballVertical * 1.5;
            player.setVelocity(horizontalVector.setY(y));
            if (source.getUniqueId()!=player.getUniqueId())
                player.setLastDamageCause(new EntityDamageByEntityEvent(source, player, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 1.0));
            if (player.equals(source)) {
                if (source.getUniqueId()==player.getUniqueId())
                    player.damage(2.0);
                else
                    player.damage(2.0,source);
            }
            /*

            Original Code:

            Vector horizontalVector = normalizedVector.multiply(fireballHorizontal);
            double y = normalizedVector.getY();
            if(y < 0 ) y += 1.5;
            if(y <= 0.5) {
                y = fireballVertical*1.5; // kb for not jumping
            } else {
                y = y*fireballVertical*1.5; // kb for jumping
            }
            player.setVelocity(horizontalVector.setY(y));
            */
            LastHit lh = LastHit.getLastHit(player);
            if (lh != null) {
                lh.setDamager(source);
                lh.setTime(System.currentTimeMillis());
            } else {
                new LastHit(player, source, System.currentTimeMillis());
            }

            if(player.equals(source)) {
                player.damage(2.0);
            } else {
                player.damage(2.0);
            }
        }
    }


    @EventHandler
    public void fireballDirectHit(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Fireball)) return;
        if(!(e.getEntity() instanceof Player)) return;

        if(Arena.getArenaByPlayer((Player) e.getEntity()) == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void fireballPrime(ExplosionPrimeEvent e) {
        if(!(e.getEntity() instanceof Fireball)) return;
        ProjectileSource shooter = ((Fireball)e.getEntity()).getShooter();
        if(!(shooter instanceof Player)) return;
        Player player = (Player) shooter;

        if(Arena.getArenaByPlayer(player) == null) return;
        e.setFire(true);
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        BlockIgniteEvent.IgniteCause cause = e.getCause();
        if (cause == BlockIgniteEvent.IgniteCause.SPREAD || cause == BlockIgniteEvent.IgniteCause.LAVA || cause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
        }
    }
}
