 <?php
 
class DB_Functions {
 
    private $db;
 
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
    //Saving new user
    public function storeUser($email, $gender, $year, $password) {
        $uuid = uniqid('', true);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt

	//saving user	
	$result = mysqli_query($GLOBALS["___mysqli_ston"], "INSERT INTO users(unique_id, email, gender, year, encrypted_password, salt, created_at) VALUES('$uuid', '$email', '$gender', '$year', '$encrypted_password', '$salt', NOW())");

        //last id
	$uid = ((is_null($___mysqli_res = mysqli_insert_id($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res);

	//setting up a table for the user's locations
	$sql = mysqli_query($GLOBALS["___mysqli_ston"], "CREATE TABLE loc".$uid."(uid int(11) primary key auto_increment, lat DOUBLE, laenge DOUBLE, bearing FLOAT, speed FLOAT, time LONG)");

        

	// check for successful store
        if ($result) {
            // get user details 
            $result = mysqli_query($GLOBALS["___mysqli_ston"], "SELECT * FROM users WHERE uid = $uid");
            // return user details
            return mysqli_fetch_array($result);
        } else {
            return false;
        }
    }
 
    //Finding user by e-mail and password
    public function getUserByEmailAndPassword($email, $password) {
        $result = mysqli_query($GLOBALS["___mysqli_ston"], "SELECT * FROM users WHERE email = '$email'") or die(((is_object($GLOBALS["___mysqli_ston"])) ? mysqli_error($GLOBALS["___mysqli_ston"]) : (($___mysqli_res = mysqli_connect_error()) ? $___mysqli_res : false)));
        // check for result 
        $no_of_rows = mysqli_num_rows($result);
        if ($no_of_rows > 0) {
            $result = mysqli_fetch_array($result);
            $salt = $result['salt'];
            $encrypted_password = $result['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $result;
            }
        } else {
            // user not found
            return false;
        }
    }
 
    //Check if user already exists
    public function isUserExisted($email) {
        $result = mysqli_query($GLOBALS["___mysqli_ston"], "SELECT email from users WHERE email = '$email'");
        $no_of_rows = mysqli_num_rows($result);
        if ($no_of_rows > 0) {
            // User exists. 
            return true;
        } else {
            // User doesn't exist. 
            return false;
        }
    }
 
    //Encrypting password
    public function hashSSHA($password) {
 
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
 
    //Decrypting password
    public function checkhashSSHA($salt, $password) {
 
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
        return $hash;
    }

    //Saving Locations
    public function storeLocations($loctable, $values) {

      $loc = mysqli_query($GLOBALS["___mysqli_ston"], "INSERT INTO ".$loctable." (lat, lon, bearing, speed, time)  VALUES ".$values);
      
    }
 
}
 
?> 