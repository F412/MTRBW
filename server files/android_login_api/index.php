 <?php
 
/**
 * File to handle all API requests
 * Accepts GET and POST
 * 
 * Each request will be identified by TAG
 * Response will be JSON data
 
  /**
 * check for POST request 
 */
if (isset($_POST['tag']) && $_POST['tag'] != '') {
    // get tag
    $tag = $_POST['tag'];
 
    // include db handler
    require_once 'include/DB_Functions.php';
    $db = new DB_Functions();
 
    // response Array
    $response = array("tag" => $tag, "error" => FALSE);
 
    // check for tag type
    if ($tag == 'login') {
        // Request type is check Login
        $email = $_POST['email'];
        $password = $_POST['password'];
 
        // check for user
        $user = $db->getUserByEmailAndPassword($email, $password);
        if ($user != false) {
            // user found
            $response["error"] = FALSE;
            $response["uid"] = $user["unique_id"];
            $response["user"]["id"] = $user["uid"];
            $response["user"]["email"] = $user["email"];
            $response["user"]["gender"] = $user["gender"];
            $response["user"]["year"] = $user["year"];
            $response["user"]["created_at"] = $user["created_at"];
            $response["user"]["updated_at"] = $user["updated_at"];
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = TRUE;
            $response["error_msg"] = "Wrong e-mail address or password";
            echo json_encode($response);
        }
    } else if ($tag == 'register') {
        // Request type is Register new user
        $email = $_POST['email'];
	$gender = $_POST['gender'];
	$year = $_POST['year'];
        $password = $_POST['password'];

 
        // Check if user already exists
        if ($db->isUserExisted($email)) {
            // user is already existed - error response
            $response["error"] = TRUE;
            $response["error_msg"] = "This e-mail address is already registered.";
            echo json_encode($response);
        } else {
            // store user
            $user = $db->storeUser($email, $gender, $year, $password);
            if ($user) {
                // user stored successfully
                $response["error"] = FALSE;
                $response["uid"] = $user["unique_id"];
		$response["user"]["id"] = $user["uid"];
                $response["user"]["email"] = $user["email"];
		$response["user"]["gender"] = $user["gender"];
		$response["user"]["year"] = $user["year"];
		$response["user"]["id"] = $user["uid"];
                $response["user"]["created_at"] = $user["created_at"];
                $response["user"]["updated_at"] = $user["updated_at"];
                echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = TRUE;
                $response["error_msg"] = "An error occured during registration";
                echo json_encode($response);
            }
        }
    } else if ($tag == 'locations') {

        // Request type is 'Transmitting Locations'
        $loctable = $_POST['loctable'];
        $values = $_POST['values'];
        
 
        // Saving locations
        $locs = $db->storeLocations($loctable, $values);
        $response["error"] = FALSE;
        $response["loctable"] = $loctable;
        echo json_encode($response);

       
    } else {
        // No valid tag has been send.
        $response["error"] = TRUE;
        $response["error_msg"] = "Unknown tag: 'login', 'register' or 'locations' is required.";
        echo json_encode($response);
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "No tag has been transmitted.";
    echo json_encode($response);
}
?> 