import React, { Component } from "react";
import AdminNav from "./AdminNav";
import { Table, Card, CardDeck, CardColumns } from "react-bootstrap";
import ApiService from "../../api/ApiService";
import BookingCard from "../BookingCard";

class DashboardAdmin extends Component {
  constructor(props) {
    super(props);
    this.state = {
      bookings: [],
      message: null,
    };
  }

  componentDidMount() {
    this.reloadBookingList();
  }

  refreshPage() {
    window.location.reload(false);
  }

  reloadBookingList = () => {
    ApiService.fetchBookings(this).then((res) => {
      this.setState({ bookings: Array.from(res.data.body.bookings) });
      debugger;
    });
  };

  approveBooking = (id) => {
    ApiService.approveBookingStatus(this, { id }).then((res) => {
      this.setState({ message: res.data });
      this.reloadBookingList();
    });
  };

  render() {
    return (
      <>
        <header>
          <AdminNav />
        </header>
        <div className="active-booking-wrapper">
          <CardColumns>
            {this.state.bookings.map((booking) => (
              <BookingCard
                key={booking.bookingID}
                booking={booking}
                render={() => (
                  <div>
                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={() => alert("Declining bookings unsupported")}
                      style={{ float: "right", margin: "5px" }}
                    >
                      Reject
                    </button>
                    <button
                      type="button"
                      className="btn btn-success"
                      onClick={() => this.approveBooking(booking.bookingID)}
                      style={{ float: "right", margin: "5px" }}
                    >
                      Approve
                    </button>
                  </div>
                )}
              />
            ))}
          </CardColumns>
        </div>
      </>
    );
  }
}

export default DashboardAdmin;
