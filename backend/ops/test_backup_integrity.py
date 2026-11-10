import io,pathlib,subprocess,sys,tarfile,tempfile,unittest
VERIFY=pathlib.Path(__file__).with_name('verify-backup.py')
class BackupIntegrityTest(unittest.TestCase):
 def make(self,root,unsafe=False):
  (root/'database.dump').write_bytes(b'PGDMPtest')
  with tarfile.open(root/'media.tar','w') as out:
   data=b'private JPEG bytes';entry=tarfile.TarInfo('../escape' if unsafe else 'photo.jpg');entry.size=len(data);out.addfile(entry,io.BytesIO(data))
 def run_verify(self,root,create=False):return subprocess.run([sys.executable,str(VERIFY),*(['--create'] if create else []),str(root)],capture_output=True)
 def test_complete_artifacts_verify_and_mutation_fails(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);self.make(root);self.assertEqual(0,self.run_verify(root,True).returncode);self.assertEqual(0,self.run_verify(root).returncode);(root/'database.dump').write_bytes(b'PGDMPmodified');self.assertNotEqual(0,self.run_verify(root).returncode)
 def test_partial_backup_is_not_usable(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);self.make(root);self.assertNotEqual(0,self.run_verify(root).returncode)
 def test_archive_traversal_cannot_be_marked_complete(self):
  with tempfile.TemporaryDirectory() as folder:
   root=pathlib.Path(folder);self.make(root,True);self.assertNotEqual(0,self.run_verify(root,True).returncode);self.assertFalse((root/'COMPLETE').exists())
if __name__=='__main__':unittest.main()
