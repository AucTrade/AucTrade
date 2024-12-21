
    $(document).ready(function () {
        $("#header").load("/header #header", function() {makeHeaderTab();});
        $("#footer").load("/footer #footer");
    });

     function makeHeaderTab(cookieName) {
            document.querySelector('#mytab-username').innerHTML = getUsernameFromCookie('Authorization');
     }

    function getUsernameFromCookie(cookieName) {
        const cookieValue = document.cookie.split('; ').find(row => row.startsWith(cookieName + '='));

        if (!cookieValue) {
            console.error('Authorization cookie not found');
            return;
        }

        const tokenVal = cookieValue.split('=')[1];
        const base64Url = tokenVal.replace('Bearer ', '').split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`).join(''));
        return JSON.parse(jsonPayload).sub
    }

    function getAjaxRequest(requestUrl) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: requestUrl,
                method: 'GET',
                contentType: "application/json; charset=UTF-8",
                success: function(data) {resolve(data);},
                error: function(error) {reject(error);}
            });
        });
    }

    function postAjaxRequest(requestUrl, obj) {
        return new Promise((resolve, reject) => {
            $.ajax({
                    url: requestUrl,
                    method: 'POST',
                    contentType: "application/json; charset=UTF-8",
                    data: JSON.stringify(obj),
                    success: function(data) {resolve(data);},
                    error: function(error) {reject(error);}
                });
            });
    }

    function postPathAjaxRequest(requestUrl) {
         return new Promise((resolve, reject) => {
              $.ajax({
                     url: requestUrl,
                     method: 'POST',
                     contentType: "application/json; charset=UTF-8",
                     success: function(data) {resolve(data);},
                     error: function(error) {reject(error);}
              });
         });
    }

    function formAjaxRequest(requestUrl, formData) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: requestUrl,
                type : 'post',
                data: formData,
                processData: false,
                contentType: false,
                success: function(result){ resolve(result);},
                error: function(xhr, status, error) { reject(error);}
            });
        });
    }

    function getUrlParams() {
       var params = {};
       window.location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi,
           function(str, key, value) {
               params[key] = value;
           }
       );
       return params;
    }