-- Images remain private on disk; photo_path is a server-generated random filename only.
ALTER TABLE event_guestbook ADD COLUMN photo_path VARCHAR(100);
