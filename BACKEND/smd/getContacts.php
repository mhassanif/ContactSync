<?php
include "connection.php";
$response=array();

$quuery="SELECT * FROM `Users` ORDER BY `Users`.`name` ASC;";
$result=mysqli_query($con,$quuery);
if($result)
{
    $response["status"]= 1;
    $response["message"]= "Data fetched successfully";
    $response["data"]=array();
    while($row=mysqli_fetch_assoc($result))
    {
        array_push($response["data"],$row);
    }
}
else{
    $response["status"]= 0;
    $response["message"]= "Data not fetched";
}

echo json_encode($response);
?>