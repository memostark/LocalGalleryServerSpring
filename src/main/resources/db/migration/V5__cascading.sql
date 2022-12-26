ALTER TABLE media_file
DROP FOREIGN KEY FK_MEDIAFILE_ON_FOLDER;
ALTER TABLE media_file
ADD CONSTRAINT FK_MEDIAFILE_ON_FOLDER FOREIGN KEY (folder_id) REFERENCES media_folder (id) ON DELETE CASCADE;