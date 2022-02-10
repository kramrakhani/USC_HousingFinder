myStorage = window.sessionStorage;
  updateAvatar();
  
  function updateAvatar() {
    if (myStorage.getItem("userName")) {
      document.querySelector("#avatarbox").classList.remove("d-none");
      if (window.location.href == "./createpost.html") {
		  document.querySelector("#send_email").classList.remove("d-none");
		  }
      document.querySelector("#userNameHolder").innerText = myStorage.getItem("userName");
      document.querySelector("#userImage").src = myStorage.getItem("userImage");
      document.querySelector("#signin").classList.add("d-none");
      document.querySelector("#createLink").classList.remove("d-none");
    } else {
      document.querySelector("#avatarbox").classList.add("d-none");
      if (window.location.href == "./createpost.html") {
		  document.querySelector("#send_email").classList.add("d-none");
		  }
		  document.querySelector("#createLink").classList.add("d-none");
      document.querySelector("#signin").classList.remove("d-none");
    }
  }
  
  function onSignIn(googleUser) {
    var profile = googleUser.getBasicProfile();
 
    myStorage.setItem('userEmail', profile.getEmail());
    myStorage.setItem('userName', profile.getName());
    myStorage.setItem('userImage', profile.getImageUrl());

    fetch('./UserRequest?email='+ profile.getEmail() + "&name=" + profile.getName(), {
    	method: 'POST',
    }).then(response => response.json())
    .then((responseJson) => {
    	myStorage.setItem('userId', responseJson.user_id);
		 updateAvatar();
    });
  }

  function signOut() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
      console.log('User signed out.');
    });
    myStorage.clear();
    updateAvatar();
  }
