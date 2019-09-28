(function(){
	
//	fake user
	var user_id = '00000';
	var user_fullname = '';
	var lng = 103.93;
	var lat = 100.34;
	
//	main funtion entrance
	init();
	
	function init() {
		// Register event listeners
		$('login-btn').addEventListener('click', login);
	    $('sign-up-btn').addEventListener('click', signUp);
	    $('sign-btn').addEventListener('click', showRegisterForm);
	    $('sign-up-link').addEventListener('click', showRegisterForm);
	    $('login-link').addEventListener('click', showLoginForm);
	    
		$('near_by').addEventListener('click', loadNearbyItems);
		$('Favorite').addEventListener('click', loadFavoriteItems);
		$('recommend').addEventListener('click', loadRecommendedItems);
		
		// validateSession();
	    var loginForm = $('login-form');
	    var registerForm = $('register-form');
	    var logoutBtn = $('logout-link');
	    
	    hideElement(loginForm);
	    hideElement(registerForm);
	    hideElement(logoutBtn);
		
		var welcomeMsg = $('welcome-msg');
        welcomeMsg.innerHTML = 'Welcome! ' + user_fullname;
        
        		// step 7
        initGeoLocation();
	}
	
	 /**
	  * Session
	  */
	  function validateSession() {
	    var url = './login';
	    var req = JSON.stringify();
	    
	    showLoadingMessage('Validating Session');
	    
	    // make AJAX Call
	    ajax('GET', url, req, 
	    function(res) {
	      var result = JSON.parse(res);
	      
	      if (result.result === 'SUCCESS') {
	    	console.log("login success")
	        onSessionValid(result);
	      }
	    });
	  }
	  
	  function onSessionValid(result) {
	    user_id = result.user_id;
	    user_fullname = result.name;
	    
	    var loginForm = $('login-form');
	    var registerForm = $('register-form');
	    var itemNav = $('side_nav');
	    var itemList = $('event_list');
	    var avatar = $('avatar');
	    var welcomeMsg = $('welcome-msg');
	    var logoutBtn = $('logout-link');
	    var signUpBtn = $('sign-up-link');
	    var loginBtn = $('login-link');
	    
	    
	    welcomeMsg.innerHTML = 'Welcome! ' + user_fullname;
	    
	    showElement(itemNav);
	    showElement(itemList);
	    showElement(avatar, 'inline-block');
	    showElement(welcomeMsg, 'inline-block');
	    showElement(logoutBtn, 'inline-block');
	    hideElement(signUpBtn);
	    hideElement(loginBtn);
	    hideElement(loginForm);
	    
	    hideElement(registerForm);
	    
//	    initGeoLocation();
	  }
	  
	  function onSessionInvalid() {
		    var loginForm = $('login-form');
		    var registerForm = $('register-form');
		    var itemNav = $('side_nav');
		    var itemList = $('event_list');
		    var avatar = $('avatar');
		    var welcomeMsg = $('welcome-msg');
		    var logoutBtn = $('logout-link');
		    var signUpBtn = $('sign-up-link');
		    var loginBtn = $('login-link');
		    
		    hideElement(itemNav);
		    hideElement(itemList);
		    hideElement(avatar);
		    hideElement(welcomeMsg);
		    hideElement(logoutBtn);
		    showElement(signUpBtn, 'inline-block');
		    showElement(loginBtn, 'inline-block');
		    showElement(loginForm);
		    
		    hideElement(registerForm);
		  }
	  
	  function hideElement(element) {
		    element.style.display = 'none';
	  }
	  function showElement(element, style) {
		    var displayStyle = style ? style : 'block';
		    element.style.display = displayStyle;
	  }
		  
	
	/** initGeoLocation function **/
	  function initGeoLocation() {
		  if (navigator.geolocation) { //browser API: navigator
			// step 8
			navigator.geolocation.getCurrentPosition(onPositionUpdated,
					onLoadPositionFailed, {
						maximumAge : 60000
					});
			showLoadingMessage('Retrieving your location...');
		  } else {
			// step 9
			onLoadPositionFailed();
		  }
	  }
	
	
	/**
     * A helper function that creates a DOM element <tag options...>
     * 
     * @param tag
     * @param options
     * @returns
     */
	function $(tag, options) {
		if (!options) {
			return document.getElementById(tag);
		}
		var element = document.createElement(tag);

		for ( var option in options) {
			if (options.hasOwnProperty(option)) {
				element[option] = options[option];
			}
		}
		return element;
	}
	
	/**
     * AJAX helper
     * 
     * @param method -
     *            GET|POST|PUT|DELETE
     * @param url -
     *            API end point
     * @param callback -
     *            This the successful callback
     * @param errorHandler -
     *            This is the failed callback
     */
	function ajax(method, url, data, callback, errorHandler) {
		var xhr = new XMLHttpRequest();

		xhr.open(method, url, true);

		xhr.onload = function() {
			if (xhr.status === 200) {
				callback(xhr.responseText);
			} else if (xhr.status === 403) {
				onSessionInvalid();
			} else {
				errorHandler();
			}
		};

		xhr.onerror = function() {
			console.error("The request couldn't be completed.");
			errorHandler();
		};

		if (data === null) {
			xhr.send();
		} else {
			xhr.setRequestHeader("Content-Type",
					"application/json;charset=utf-8");
			xhr.send(data);
		}
	}
	
	
	// -----------------------------------
    // Helper Functions
    // -----------------------------------

    /**
     * A helper function that makes a navigation button active
     * 
     * @param btnId -
     *            The id of the navigation button
     */
	/** step 5: onPositionUpdated function in initGeoLocation **/
	function onPositionUpdated(position) {
		lat = position.coords.latitude;
		lng = position.coords.longitude;

		loadNearbyItems();
	}
	
	/** step 6: onPositionFailed function **/
	function onLoadPositionFailed() {
		console.warn('navigator.geolocation is not available');
		
		getLocationFromIP();
	}
	
	/** showLoadingMessage function **/
	function showLoadingMessage(msg) {
		var itemList = $('event_list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> '
				+ msg + '</p>';
	}
	
	/** showWarningMessage function **/
	function showWarningMessage(msg) {
		var itemList = $('event_list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
				+ msg + '</p>';
	}
	/** showErrorMessage function **/
	function showErrorMessage(msg) {
		var itemList = $('event_list');
		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
				+ msg + '</p>';
	}


	
	
	/** step 8: activeBtn function **/
	
	/**
	 * A helper function that makes a navigation button active
	 * 
	 * @param btnId - The id of the navigation button
	 */
	function activeBtn(btnId) {
		var btns = document.getElementsByClassName('main-nav-btn');

		// deactivate all navigation buttons
		for (var i = 0; i < btns.length; i++) {
			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
		}

		// active the one that has id = btnId
		var btn = $(btnId);
		btn.className += ' active';
	}
	
	
	/** getLocationFromIP function **/
	function getLocationFromIP() {
		// Get location from http://ipinfo.io/json
		var url = 'http://ipinfo.io/json'
		var req = null;
		ajax('GET', url, req, function(res) {
			var result = JSON.parse(res);
			if ('loc' in result) {
				var loc = result.loc.split(',');
				lat = loc[0];
				lng = loc[1];
			} else {
				console.warn('Getting location by IP failed.');
			}
			// step 7
			loadNearbyItems();
		});
	}
	
	//----------------list the result--------------------------------------
	/** show the results : listItems function **/
	/**
	 * @param items - An array of item JSON objects
	 */
    function listItems(items) {
		// Clear the current results
		var itemList = $('event_list');
		itemList.innerHTML = '';

		for (var i = 0; i < items.length; i++) {
			addItem(itemList, items[i]);
		}
	}
	
	
	/**
	 * Add item to the list
	 * @param itemList - The <ul id="item-list"> tag
	 * @param item - The item data (JSON object)
	 */
	function addItem(itemList, item) {
		var item_id = item.item_id;

		// create the <li> tag and specify the id and class attributes
		var li = $('li', {
			id : 'event-' + item_id,
			className : 'event'
		});

		// set the data attribute
		li.dataset.item_id = item_id;
		li.dataset.favorite = item.favorite;

		// item image
		if (item.image_url) {
			li.appendChild($('img', {
				src : item.image_url
			}));
		} else {
			li.appendChild($('img', {
				src : 'https://www.umthunzi.co.za/2016/wp-content/uploads/2017/10/fotolia_169065284.jpg'
			}))
		}
		// section
		var section = $('div', {});

		// title
		var title = $('a', {
			href : item.url,
			target : '_blank',
			className : 'event_name'
		});
		title.innerHTML = item.name;
		section.appendChild(title);

		// category
		var category = $('p', {
			className : 'event_category'
		});
		category.innerHTML = 'Category: ' + item.categories.join(', ');
		section.appendChild(category);

//		var stars = $('div', {
//			className : 'stars'
//		});
		
//		for (var i = 0; i < item.rating; i++) {
//			var star = $('i', {
//				className : 'fa fa-star'
//			});
//			stars.appendChild(star);
//		}
//
//		if (('' + item.rating).match(/\.5$/)) {
//			stars.appendChild($('i', {
//				className : 'fa fa-star-half-o'
//			}));
//		}
//
//		section.appendChild(stars);

		li.appendChild(section);

		// address
		var address = $('p', {
			className : 'event_address'
		});

		address.innerHTML = item.address.replace(/n,/g, ',').replace(/\"/g,
				' ');
		li.appendChild(address);

		// favorite link
		var favLink = $('p', {
			className : 'favor-icon'
		});

		favLink.onclick = function() {
			changeFavoriteItem(item_id);
		};
		
		favLink.appendChild($('i', {
			id : 'fav-icon-' + item_id,
			className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
		}));

		li.appendChild(favLink);

		itemList.appendChild(li);
	}
	
	// -----------------------------------
    // Helper end
    // -----------------------------------


	// -------------------------------------
    // AJAX call server-side APIs
    // -------------------------------------

    /**
     * API #1 Load the nearby items API end point: [GET]
     * /Eventrecommend/search?user_id=1111&lat=37.38&lon=-122.08
     */
	function loadNearbyItems() {
		console.log('loadNearbyItems');
		// step 8
		activeBtn('near_by');

		// The request parameters
		var url = './search';
		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
		var req = JSON.stringify({});
		
		
		showLoadingMessage('Loading nearby items...');

		// make AJAX call
		ajax('GET', url + '?' + params, req,
		// successful callback
		function(res) {
			var items = JSON.parse(res);
			if (!items || items.length === 0) {
				
				showWarningMessage('No nearby item.');
			} else {
				
				listItems(items);
			}
		},
		// failed callback
		function() {
			
			showErrorMessage('Cannot load nearby items.');
		});
	}
	
	/**
     * API #2 Toggle favorite (or visited) items
     * 
     * @param item_id -
     *            The item business id
     * 
     * API end point: [POST]/[DELETE] /Dashi/history request json data: {
     * user_id: 00000, visited: [a_list_of_business_ids] }
     */
    function changeFavoriteItem(item_id) {
        // Check whether this item has been visited or not
        var li = $('event-' + item_id);
        var favIcon = $('fav-icon-' + item_id);
        var favorite = li.dataset.favorite !== 'true';

        // The request parameters
        var url = './history';
        var req = JSON.stringify({
            user_id: user_id,
            favorite: [item_id]
        });
        var method = favorite ? 'POST' : 'DELETE';

        ajax(method, url, req,
            // successful callback
            function(res) {
                var result = JSON.parse(res);
                if (result.result === 'SUCCESS') {
                    li.dataset.favorite = favorite;
                    favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
                }else {
                    favIcon.innerHTML = 'lalala';
                }
           	},
                   // failed callback
            function() {
                showErrorMessage('Cannot set favorite items.');
            });
    }
    
    /**
     * API #3 Load favorite (or visited) items API end point: [GET]
     * /Eventrecommend/history?user_id=1111
     */
    function loadFavoriteItems() {
        activeBtn('Favorite');

        // The request parameters
        var url = './history';
        var params = 'user_id=' + user_id;
        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading favorite items...');

        // make AJAX call
        ajax('GET', url + '?' + params, req, function(res) {
            var items = JSON.parse(res);
            if (!items || items.length === 0) {
                showWarningMessage('No favorite item.');
            } else {
                listItems(items);
            }
        }, function() {
            showErrorMessage('Cannot load favorite items.');
        });
    }
    
    /**
     * API #4 Load recommended items API end point: [GET]
     * /Eventrecommend/recommend?user_id=1111
     */
    function loadRecommendedItems() {
        activeBtn('recommend');

        // The request parameters
        var url = './recommend';
        var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;

        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading recommended items...');

        // make AJAX call
        ajax(
            'GET',
            url + '?' + params,
            req,
            // successful callback
            function(res) {
                var items = JSON.parse(res);
                if (!items || items.length === 0) {
                    showWarningMessage('No recommended item. Make sure you have favorites.');
                } else {
                    listItems(items);
                }
            },
            // failed callback
            function() {
                showErrorMessage('Cannot load recommended items.');
            });
    }
    
    
	
    /**
     * API #5 Login: [POST]
     * /Eventrecommend/login?user_id=1111&password=
     */
    function login() {
    	var username = $('username').value;
    	var password = $('password').value;
    
    //password = md5(username + md5(password));
    
    	var url = './login';
    	var req = JSON.stringify({
    		user_id : username,
    		password : password,
    	});
    	
    	ajax('POST', url, req,
    			function(res) {
    		var result = JSON.parse(res);
      
    		if (result.result === 'SUCCESS') {
    			console.log("login success")
    			onSessionValid(result);
    		}
    	},
    	function() {
    		showLoginError();
    	});
    }
  
    function showLoginError() {
    	$('login-error').innerHTML = 'Invalid username or password';
    }
  
    function clearLoginError() {
    	$('login-error').innerHTML = '';
    }
  
    /**
     * API #6 register/ signup: [GET]
     * /Eventrecommend/register?
     */
    function signUp() {
    	var loginForm = $('login-form');
    	var registerForm = $('register-form');
    	var itemNav = $('side_nav');
    	var itemList = $('event_list');
    	var avatar = $('avatar');
    	var welcomeMsg = $('welcome-msg');
    	var logoutBtn = $('logout-link');
    	var signUpBtn = $('sign-up-link');

    	hideElement(itemNav);
    	hideElement(itemList);
    	hideElement(avatar);
    	hideElement(welcomeMsg);
    	hideElement(logoutBtn);
    	showElement(signUpBtn, 'inline-block');
    	hideElement(loginForm);
  
    	showElement(registerForm);
  
  
    	var username = $('username-signUp').value;
    	var password = $('password-signUp').value;
    	var firstname = $('firstname').value;
    	var lastname = $('lastname').value;

    	// password = md5(username + md5(password));

    	var url = './register';
    	var req = JSON.stringify({
    		user_id : username,
    		password : password,
    		first_name : firstname,
    		last_name : lastname,
    	});

    	ajax('POST', url, req,
    			function(res) {
    		var result = JSON.parse(res);

    		if (result.result === 'SUCCESS') {
    			console.log("sign up success!")
    			onSessionValid(result);
    		}
//    		//for debbug
//    		if (result.result === "The name has already been used."){
//    			console.log("truth is The name has already been used.")
//    		}
    	},
         	function() {
    		showRegisterError();
    	});
    }
  
    function showRegisterError() {
    	$('register-error').innerHTML = 'The username has been token.';
    }
    
    /**-------------------------------------------------
     * LOGIN/REGISTER helper function 
     * 
     */
  
    function showRegisterForm() {
	    var loginForm = $('login-form');
	    var registerForm = $('register-form');
	    var itemNav = $('side_nav');
	    var itemList = $('event_list');
	    var avatar = $('avatar');
	    var welcomeMsg = $('welcome-msg');
	    var logoutBtn = $('logout-link');
	    var signUpBtn = $('sign-up-link');

	    hideElement(itemNav);
	    hideElement(itemList);
	    hideElement(avatar);
	    hideElement(welcomeMsg);
	    hideElement(logoutBtn);
	    showElement(signUpBtn, 'inline-block');
	    hideElement(loginForm);

	    showElement(registerForm);
  }
  
  function showLoginForm() {
		var loginForm = $('login-form');
		var registerForm = $('register-form');
		var itemNav = $('side_nav');
		var itemList = $('event_list');
		var avatar = $('avatar');
		var welcomeMsg = $('welcome-msg');
		var logoutBtn = $('logout-link');
		var signUpBtn = $('sign-up-link');

		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(welcomeMsg);
		hideElement(logoutBtn);
		showElement(signUpBtn, 'inline-block');
		showElement(loginForm);

		hideElement(registerForm);
  }
	
	












	
})();
